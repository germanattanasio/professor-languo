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
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Vote;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Votes;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;

/**
 * Unit tests of the ability to unmarshal a StackExchange Votes.xml file, translating each row of
 * the file into a distinct {@link Vote} object, populating the relevant fields
 *
 */
public class VotesTest {

  VotesTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private final static Logger logger = LogManager.getLogger(VotesTest.class.getName());

  private File inputVotesFile;
  private List<Vote> referenceVotes;
  private Votes unmarshalledVotes;

  private Map<Integer, Set<Vote>> referenceIdToVotesMap;

  /**
   * This is not an actual unit test, but rather just a way to invoke unmarshalling of a complete
   * Votes.xml file
   */
  // @Test
  public void test_import_complete_votes_xml_file() throws JAXBException {
    File file = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleVotes.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(Votes.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    Votes votes = (Votes) jaxbUnmarshaller.unmarshal(file);
    logger.info("# of votes unmarshalled: " + votes.getVotes().size());
    int i = 1;
    for (Vote vote : votes.getVotes()) {
      logger.info("Vote " + i + "\n" + vote.toString());
      i++;
    }
  }

  @Test public void test_that_votes_xml_is_unmarshalled_correctly() throws JAXBException {
    GIVEN.input_votes_xml_file_is_created();
    AND.reference_votes_are_created();
    WHEN.votes_xml_file_is_unmarshalled();
    THEN.unmarshalled_votes_match_reference_votes();
  }

  @Test public void test_that_votes_can_be_extracted_by_post_id() throws JAXBException {
    GIVEN.input_votes_xml_file_is_created();
    AND.reference_votes_are_created();
    WHEN.votes_xml_file_is_unmarshalled();
    THEN.reference_votes_can_be_extracted_by_post_id();
  }

  @Test public void test_that_deserialized_vote_equals_original_object() throws JAXBException, IngestionException {
    GIVEN.input_votes_xml_file_is_created();
    WHEN.votes_xml_file_is_unmarshalled();
    THEN.unmarshalled_votes_should_match_deserialized_votes();

  }

  private void unmarshalled_votes_should_match_deserialized_votes() throws IngestionException {
    for (Vote vote : unmarshalledVotes.getVotes()) {
      byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(vote);
      Vote deserializedVote = (Vote) StackExchangeThreadSerializer.deserializeObjFromBinArr(binCode);
      assertTrue(vote.equals(deserializedVote));
      assertEquals(vote.hashCode(), deserializedVote.hashCode());
    }
  }

  /**
   * getVotesByPostId should return exactly the same set of Votes as the set of Votes from the
   * referenceVotes. Not only about the number of votes, each individual vote should be the same.
   * 
   */
  private void reference_votes_can_be_extracted_by_post_id() {
    Set<Vote> builtSetOfVotes, referenceSetOfVotes;
    for (Vote vote : referenceVotes) {
      Integer postId = vote.getPostId();
      builtSetOfVotes = unmarshalledVotes.getVotesByPostId(postId);
      referenceSetOfVotes = referenceIdToVotesMap.get(postId);
      assertTrue("built set of posts doesn't match the referenced set of posts with the same parent id: " + postId,
          builtSetOfVotes.equals(referenceSetOfVotes));;
    }
  }

  private void unmarshalled_votes_match_reference_votes() {
    for (int i = 0; i < referenceVotes.size(); i++) {
      assertTrue("Mismatch between unmarshalled vote " + i + " and reference vote " + i,
          unmarshalledVotes.getVotes().get(i).equals(referenceVotes.get(i)));
    }
  }

  private void votes_xml_file_is_unmarshalled() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Votes.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    unmarshalledVotes = (Votes) jaxbUnmarshaller.unmarshal(inputVotesFile);
    unmarshalledVotes.setSite("robotics.stackexchange.com");
  }

  private void reference_votes_are_created() {
    DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

    Vote vote0 = new Vote();
    vote0.id = 1;
    vote0.postId = 6715;
    vote0.voteType = VoteType.UP_MOD;
    vote0.creationDate = fmt.parseDateTime("2012-10-23T00:00:00.000");

    Vote vote1 = new Vote();
    vote1.id = 2;
    vote1.postId = 6715;
    vote1.voteType = VoteType.UP_MOD;
    vote1.creationDate = fmt.parseDateTime("2012-10-24T00:00:00.000");

    Vote vote2 = new Vote();
    vote2.id = 3;
    vote2.postId = 6715;
    vote2.voteType = VoteType.DOWN_MOD;
    vote2.creationDate = fmt.parseDateTime("2012-10-23T00:00:00.000");

    referenceVotes = Arrays.asList(vote0, vote1, vote2);

    referenceIdToVotesMap = new HashMap<Integer, Set<Vote>>();
    referenceIdToVotesMap.put(vote0.postId, new HashSet<Vote>(Arrays.asList(vote0, vote1, vote2)));
  }

  private void input_votes_xml_file_is_created() {
    inputVotesFile = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleVotes.xml");
  }

}
