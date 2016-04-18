/*
 * Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.PostLink;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.PostLinks;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.LinkType;

/**
 * Unit tests of the ability to unmarshal a StackExchange PostLinks.xml file, translating each row
 * of the file into a distinct {@link PostLink} object, populating the relevant fields
 *
 */
public class PostLinksTest {

  PostLinksTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private final static Logger logger = LogManager.getLogger(PostLinksTest.class.getName());

  private File inputPostLinksFile;
  private List<PostLink> referencePostLinks;
  private PostLinks unmarshalledPostLinks;

  private Map<Integer, Set<PostLink>> referenceIdToPostLinksMap;

  /**
   * This is not an actual unit test, but rather just a way to invoke unmarshalling of a complete
   * PostLinks.xml file
   */

  // @Test
  public void test_import_complete_postLinks_xml_file() throws JAXBException {
    File file = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePostLinks.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(PostLinks.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    PostLinks postLinks = (PostLinks) jaxbUnmarshaller.unmarshal(file);
    logger.info("# of postLinks unmarshalled: " + postLinks.getPostLinks().size());
    int i = 1;
    for (PostLink postLink : postLinks.getPostLinks()) {
      logger.info("PostLink " + i + "\n" + postLink.toString());
      i++;
    }
  }

  @Test public void test_that_postLinks_xml_is_unmarshalled_correctly() throws JAXBException {
    GIVEN.input_postLinks_xml_file_is_created();
    AND.reference_postLinks_are_created();
    WHEN.postLinks_xml_file_is_unmarshalled();
    THEN.unmarshalled_postLinks_match_reference_postLinks();
  }

  @Test public void test_that_id_to_postlink_map_is_populated_correctly() throws JAXBException {
    GIVEN.input_postLinks_xml_file_is_created();
    AND.reference_postLinks_are_created();
    WHEN.postLinks_xml_file_is_unmarshalled();
    THEN.id_to_postlink_map_is_populated_correctly();
  }

  @Test public void test_that_deserialized_postlink_equals_original_object() throws JAXBException, IngestionException {
    GIVEN.input_postLinks_xml_file_is_created();
    WHEN.postLinks_xml_file_is_unmarshalled();
    THEN.unmarshalled_postlinks_should_match_deserialized_postlinks();

  }

  private void unmarshalled_postlinks_should_match_deserialized_postlinks() throws IngestionException {
    for (PostLink postlink : unmarshalledPostLinks.getPostLinks()) {
      byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(postlink);
      PostLink deserializedPostLink = (PostLink) StackExchangeThreadSerializer.deserializeObjFromBinArr(binCode);
      assertTrue(postlink.equals(deserializedPostLink));
      assertEquals(postlink.hashCode(), deserializedPostLink.hashCode());
    }
  }

  /**
   * getLinksByPostId should return from the unmarshalledPostLinks the same set of Links as those
   * from the the referencePostLinks , nothing more, nothing less.
   */
  private void id_to_postlink_map_is_populated_correctly() {
    // logger.info(referencePostLinks.equals(unmarshalledPostLinks);
    Set<PostLink> builtSetOfPostLinks, referenceSetOfPostLinks;
    for (PostLink link : referencePostLinks) {
      int postId = link.getPostId();
      // assertTrue("PostID-to-PostLink map is missing entry for postId "
      // + link.getPostId(),
      // unmarshalledPostLinks.getLinksByPostId(link.getPostId()).contains(link));
      builtSetOfPostLinks = unmarshalledPostLinks.getLinksByPostId(postId);
      referenceSetOfPostLinks = referenceIdToPostLinksMap.get(postId);
      assertTrue("built set of links doesn't match the referenced set of links with the same post id: " + postId,
          builtSetOfPostLinks.equals(referenceSetOfPostLinks));;
    }
  }

  private void unmarshalled_postLinks_match_reference_postLinks() {
    assertTrue("Mismatch between unmarshalled postLink 0 and reference postLink 0",
        unmarshalledPostLinks.getPostLinks().get(0).equals(referencePostLinks.get(0)));
    assertTrue("Mismatch between unmarshalled postLink 1 and reference postLink 1",
        unmarshalledPostLinks.getPostLinks().get(1).equals(referencePostLinks.get(1)));
  }

  private void postLinks_xml_file_is_unmarshalled() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(PostLinks.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    unmarshalledPostLinks = (PostLinks) jaxbUnmarshaller.unmarshal(inputPostLinksFile);
    unmarshalledPostLinks.setSite("robotics.stackexchange.com");
  }

  private void reference_postLinks_are_created() {
    DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

    PostLink postLink0 = new PostLink();
    postLink0.id = 101;
    postLink0.postId = 6715;
    postLink0.relatedPostId = 23;
    postLink0.linkType = LinkType.LINKED;
    postLink0.creationDate = fmt.parseDateTime("2012-10-23T21:14:50.640");

    PostLink postLink1 = new PostLink();
    postLink1.id = 346;
    postLink1.postId = 54;
    postLink1.relatedPostId = 6;
    postLink1.linkType = LinkType.DUPLICATE;
    postLink1.creationDate = fmt.parseDateTime("2012-10-23T23:47:54.710");

    PostLink postLink2 = new PostLink();
    postLink2.id = 347;
    postLink2.postId = 54;
    postLink2.relatedPostId = 16;
    postLink2.linkType = LinkType.LINKED;
    postLink2.creationDate = fmt.parseDateTime("2012-10-23T23:27:54.710");

    referencePostLinks = Arrays.asList(postLink0, postLink1, postLink2);
    referenceIdToPostLinksMap = new HashMap<Integer, Set<PostLink>>();
    referenceIdToPostLinksMap.put(postLink0.postId, new HashSet<PostLink>(Arrays.asList(postLink0)));
    referenceIdToPostLinksMap.put(postLink1.postId, new HashSet<PostLink>(Arrays.asList(postLink1, postLink2)));
  }

  private void input_postLinks_xml_file_is_created() {
    inputPostLinksFile =
        new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePostLinks.xml");
  }

}
