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
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Post;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Posts;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.PostType;

/**
 * Unit tests of the ability to unmarshal a StackExchange Posts.xml file, translating each row of
 * the file into a distinct {@link Post} object, populating the relevant fields
 *
 */
public class PostsTest {

  PostsTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private final static Logger logger = LogManager.getLogger(PostsTest.class.getName());

  private File inputPostsFile;
  private List<Post> referencePosts;
  private List<Post> referenceHTMLPosts;
  private List<Post> referenceUntaggedTargetPosts;
  private Posts unmarshalledPosts;
  private Map<Integer, Set<Post>> referenceIdToPostsMap;

  /**
   * This is not an actual unit test, but rather just a way to invoke unmarshalling of a complete
   * Posts.xml file
   */
  // @Test
  public void test_import_complete_posts_xml_file() throws JAXBException {
    File file = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePosts.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(Posts.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    Posts posts = (Posts) jaxbUnmarshaller.unmarshal(file);
    logger.info("# of posts unmarshaled: " + posts.getPosts().size());
    int i = 1;
    for (Post post : posts.getPosts()) {
      logger.info("Post " + i + "\n" + post.toString());
      i++;
    }
  }

  @Test public void test_that_posts_xml_is_unmarshalled_correctly() throws JAXBException {
    GIVEN.input_posts_xml_file_is_created();
    AND.reference_posts_are_created();
    WHEN.posts_xml_file_is_unmarshalled();
    THEN.unmarshalled_posts_match_reference_posts();
  }

  @Test public void test_that_posts_with_parent_ids_can_be_retrieved() throws JAXBException {
    GIVEN.input_posts_xml_file_is_created();
    AND.reference_posts_are_created();
    WHEN.posts_xml_file_is_unmarshalled();
    THEN.posts_with_parent_ids_can_be_retrieved();
  }

  @Test public void test_that_deserialized_post_equals_original_object() throws JAXBException, IngestionException {
    GIVEN.input_posts_xml_file_is_created();
    WHEN.posts_xml_file_is_unmarshalled();
    THEN.unmarshalled_posts_match_deserialized_posts();
  }

  @Test public void test_that_removes_html_tags_from_threads() {
    GIVEN.reference_posts_with_html_tags_are_created();
    WHEN.posts_that_contain_htmltags_are_untagged();
    THEN.untagged_posts_are_equal_to_target_posts();
  }

  private void reference_posts_with_html_tags_are_created() {
    Post p0 = new Post();
    p0.id = 0;
    p0.title = "This is some random title";
    p0.body = "<p>Who knows haha?</p><br><h1>some more html tags</h1><br><code>weird ones too</code>";
    Post t0 = new Post();
    t0.id = 0;
    t0.title = "This is some random title";
    t0.body = "Who knows haha? some more html tags weird ones too";

    Post p1 = new Post();
    p1.id = 1;
    p1.body = "<p>42</p>";
    p1.title = "<h1>Title with html tag</h1>";
    Post t1 = new Post();
    t1.id = 1;
    t1.title = "Title with html tag";
    t1.body = "42";

    Post p2 = new Post();
    p2.id = 2;
    p2.title = "What is the most delicious food on earth?";
    p2.body = "<p>Who knows?</p><br><p>Will escape characters like &#60 work?</p>";
    Post t2 = new Post();
    t2.id = 2;
    t2.title = "What is the most delicious food on earth?";
    t2.body = "Who knows? Will escape characters like < work?";

    referenceUntaggedTargetPosts = Arrays.asList(t0, t1, t2);
    referenceHTMLPosts = Arrays.asList(p0, p1, p2);
  }

  private void posts_that_contain_htmltags_are_untagged() {
    for (Post p : referenceHTMLPosts) {
      if (p.getTitle() != null && p.getBody() != null)
        if (p.getTitle().contains("<") || p.getTitle().contains(">") || p.getBody().contains("<")
            || p.getBody().contains(">")) {
          p.title = p.getUnformattedTitle();
          p.body = p.getUnformattedBody();
        }
    }
  }

  private void untagged_posts_are_equal_to_target_posts() {
    for (int i = 0; i < referenceHTMLPosts.size(); i++) {
      Post taggedPost = referenceHTMLPosts.get(i);
      Post untaggedPost = referenceUntaggedTargetPosts.get(i);
      if (taggedPost.getId() == untaggedPost.getId()) {
        assertTrue("The title of post " + taggedPost.getId() + " is incorrected! \n" + "supposed to be: "
            + taggedPost.title + "\n" + "got           : " + untaggedPost.title,
            taggedPost.title.equals(untaggedPost.title));
        assertTrue("The body of post " + taggedPost.getId() + " is incorrected! \n" + "supposed to be: "
            + taggedPost.body + "\n" + "got           : " + untaggedPost.body,
            taggedPost.body.equals(untaggedPost.body));
      } else {
        assertTrue("The lists of posts are out of order!", false);
      }

    }
  }

  private void unmarshalled_posts_match_deserialized_posts() throws IngestionException {
    for (Post post : unmarshalledPosts.getPosts()) {
      byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(post);
      Post deserializedPost = (Post) StackExchangeThreadSerializer.deserializeObjFromBinArr(binCode);
      assertTrue(post.equals(deserializedPost));
      assertEquals(post.hashCode(), deserializedPost.hashCode());
    }
  }

  /**
   * getPostsByParentId should return from unmarshalledPosts the same set of Posts as the set of
   * posts from the referenced Posts, nothing more, nothing less.
   */
  private void posts_with_parent_ids_can_be_retrieved() {
    // for (Post post : referencePosts) {
    // assertTrue(
    // "Failed to retrieve post(s) for parent ID "
    // + ((post.getParentId() == null) ? "[null]"
    // : post.getParentId()),
    // unmarshalledPosts.getPostsByParentId(post.getParentId())
    // .contains(post));
    // }

    Set<Post> builtSetOfPosts, referenceSetOfPosts;
    for (Post post : referencePosts) {
      Integer parentId = post.getParentId();
      builtSetOfPosts = unmarshalledPosts.getPostsByParentId(parentId);
      referenceSetOfPosts = referenceIdToPostsMap.get(parentId);
      assertTrue("built set of posts doesn't match the referenced set of posts with the same parent id: " + parentId,
          builtSetOfPosts.equals(referenceSetOfPosts));;
    }

  }

  private void unmarshalled_posts_match_reference_posts() {
    Post referencePost, builtPost;
    for (int i = 0; i < referencePosts.size(); i++) {
      referencePost = referencePosts.get(i);
      builtPost = unmarshalledPosts.getPosts().get(i);
      assertTrue("Mismatch between unmarshalled post " + i + " and reference post " + i,
          builtPost.equals(referencePost));
    }

  }

  private void posts_xml_file_is_unmarshalled() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Posts.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    unmarshalledPosts = (Posts) jaxbUnmarshaller.unmarshal(inputPostsFile);
    unmarshalledPosts.setSite("robotics.stackexchange.com");
  }

  private void reference_posts_are_created() {
    DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

    Post post0 = new Post();
    post0.id = 6715;
    post0.postType = PostType.QUESTION;
    post0.acceptedAnswerId = 6716;
    post0.creationDate = fmt.parseDateTime("2015-03-04T10:26:19.073");
    post0.score = 0;
    post0.viewCount = 34;
    post0.ownerUserId = 9216;
    post0.answerCount = 2;
    post0.lastActivityDate = fmt.parseDateTime("2015-03-06T09:01:57.130");
    post0.title = "What is the answer to life the universe and everything?";
    post0.tags = Arrays.asList("life", "meaning", "existence");
    post0.commentCount = 0;
    post0.body = "<p>I would really like to know.</p>";

    Post post1 = new Post();
    post1.id = 6716;
    post1.postType = PostType.ANSWER;
    post1.parentId = 6715;
    post1.creationDate = fmt.parseDateTime("2015-03-04T12:08:29.380");
    post1.score = 1;
    post1.ownerUserId = 9214;
    post1.lastEditorUserId = 9214;
    post1.lastEditDate = fmt.parseDateTime("2015-03-05T05:57:11.410");
    post1.commentCount = 1;
    post1.lastActivityDate = fmt.parseDateTime("2015-03-05T05:57:11.410");
    post1.body = "<p>42</p>";

    Post post2 = new Post();
    post2.id = 6717;
    post2.postType = PostType.ANSWER;
    post2.parentId = 6715;
    post2.creationDate = fmt.parseDateTime("2015-03-05T12:08:29.380");
    post2.score = 0;
    post2.ownerUserId = 9220;
    post2.lastEditorUserId = 9220;
    post2.lastEditDate = fmt.parseDateTime("2015-03-06T05:57:11.410");
    post2.commentCount = 0;
    post2.lastActivityDate = fmt.parseDateTime("2015-03-06T05:57:11.410");
    post2.body = "<p>Who knows?</p>";

    Post post3 = new Post();
    post3.id = 1;
    post3.postType = PostType.QUESTION;
    post3.parentId = null;
    post3.creationDate = fmt.parseDateTime("2012-10-23T19:38:18.867");
    post3.score = 11;
    post3.ownerUserId = 21;
    post3.lastEditorUserId = 177;
    post3.lastEditDate = fmt.parseDateTime("2012-11-12T03:17:16.247");
    post3.commentCount = 6;
    post3.lastActivityDate = fmt.parseDateTime("2012-11-12T03:17:16.247");
    post3.title = "What is the right approach to write the spin controller for a soccer robot?";
    post3.tags = Arrays.asList("soccer", "control");
    post3.body = "<p>Who knows haha?</p>";
    post3.viewCount = 154;
    post3.answerCount = 2;

    referencePosts = Arrays.asList(post0, post1, post2, post3);

    referenceIdToPostsMap = new HashMap<Integer, Set<Post>>();
    referenceIdToPostsMap.put(null, new HashSet<Post>(Arrays.asList(post0, post3)));
    referenceIdToPostsMap.put(post0.id, new HashSet<Post>(Arrays.asList(post1, post2)));

  }

  private void input_posts_xml_file_is_created() {
    inputPostsFile = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePosts.xml");
  }

}
