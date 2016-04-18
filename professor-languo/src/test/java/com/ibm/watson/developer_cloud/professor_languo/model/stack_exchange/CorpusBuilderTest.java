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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.RetrieveAndRankIndexerTest;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.CorpusBuilder;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Post;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.PostLinks;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Posts;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Users;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Votes;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.PrimarySearchConstants;

/**
 * Unit test of the ability to import data from Posts.xml, Users.xml, Votes.xml, and PostLinks.xml,
 * and then build a set of {@link StackExchangeQuestion} objects from the resultant data structures
 * using {@link CorpusBuilder}.
 *
 */
public class CorpusBuilderTest {
  CorpusBuilderTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private final static Logger logger = LogManager.getLogger(CorpusBuilderTest.class.getName());

  /******************************************
   * The following variables are used as the referenced input arguments for the functions to be
   * tested
   ******************************************/
  private static final String INPUT_POSTS_FILE_NAME =
      CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePosts.xml";
  private static final String INPUT_USERS_FILE_NAME =
      CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleUsers.xml";
  private static final String INPUT_VOTES_FILE_NAME =
      CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleVotes.xml";
  private static final String INPUT_LINKS_FILE_NAME =
      CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SamplePostLinks.xml";

  private static final File INPUT_POSTS_FILE = new File(INPUT_POSTS_FILE_NAME);
  private static final File INPUT_USERS_FILE = new File(INPUT_USERS_FILE_NAME);
  private static final File INPUT_VOTES_FILE = new File(INPUT_VOTES_FILE_NAME);
  private static final File INPUT_LINKS_FILE = new File(INPUT_LINKS_FILE_NAME);

  private static final String SITE_NAME = "robotics.stackexchange.com";

  private static Posts INPUT_POSTS = null;
  private static Users INPUT_USERS = null;
  private static Votes INPUT_VOTES = null;
  private static PostLinks INPUT_POSTLINKS = null;
  private static int INPUT_QUESTION_ID = 6715;
  private static Set<StackExchangeAnswer> ANSWER_SET = null;
  private static Set<StackExchangeThread> QUESTION_SET = null;

  @Rule public TemporaryFolder testOutputFolder = new TemporaryFolder();

  /******************************************
   * The following variables are used as the referenced return values and the acutal return values
   * from the functions to be tested. If the referenced return values are the same with the actual
   * return values, then the functions pass the test
   ******************************************/

  /**
   * referenced return value and actual return value of the function:<br>
   * {@link CorpusBuilder#buildCorpus(String, String, String, String, String)} <br>
   * and {@link CorpusBuilder#buildCorpus(File, File, File, File, String)} <br>
   * and {@link CorpusBuilder#buildStackExchangeQuestionSet(Posts, Users, Votes, PostLinks, String)}
   * <br>
   * 
   */
  private Set<StackExchangeThread> referenceCorpus = null, builtCorpus = null;

  /**
   * referenced return value and actual return value of the function:<br>
   * {@link CorpusBuilder#buildAnswersForQuestion(Integer, Posts, Users, Votes, PostLinks, String)}
   * 
   */
  private Set<StackExchangeAnswer> referenceAnswers, builtAnswers;

  private static CorpusBuilder sampleCorpusBuilder = null;

  private static CorpusBuilder dupCorpusBuilder = null;

  @BeforeClass public static void setUp() throws JAXBException {
    INPUT_POSTS = CorpusBuilder.unmarshallFile(INPUT_POSTS_FILE, Posts.class);
    INPUT_USERS = CorpusBuilder.unmarshallFile(INPUT_USERS_FILE, Users.class);
    INPUT_VOTES = CorpusBuilder.unmarshallFile(INPUT_VOTES_FILE, Votes.class);
    INPUT_POSTLINKS = CorpusBuilder.unmarshallFile(INPUT_LINKS_FILE, PostLinks.class);

  }

  /**
   * This is not an actual unit test, but rather just a way to invoke unmarshalling of a complete
   * corpus
   */

  // @Test
  public void test_import_complete_users_xml_file() throws IngestionException {
    String site = "robotics.stackexchange.com";

    sampleCorpusBuilder.buildCorpus(INPUT_POSTS_FILE_NAME, INPUT_USERS_FILE_NAME, INPUT_VOTES_FILE_NAME,
        INPUT_LINKS_FILE_NAME, site);
    Set<StackExchangeThread> uniqueThreadSet = sampleCorpusBuilder.getUniqueThreadSetFromBinFiles();

    int i = 1;
    for (StackExchangeThread question : uniqueThreadSet) {
      logger.info("Question " + i + "\n" + question.toString());
      i++;
    }
  }

  @Test public void test_that_stack_exchange_questions_can_be_created_from_xml_corpus_filename()
      throws IngestionException {
    GIVEN.reference_corpus_is_created();
    WHEN.build_corpus_from_filename_is_invoked();
    THEN.built_corpus_matches_reference();
  }

  @Test public void test_that_stack_exchange_questions_can_be_created_from_xml_corpus() throws IngestionException {
    GIVEN.reference_corpus_is_created();
    WHEN.build_corpus_is_invoked();
    THEN.built_corpus_matches_reference();
  }

  @Test public void test_that_stack_exchange_questions_can_be_created_from_posts_users_votes_postlinks_and_sitename()
      throws IngestionException {
    GIVEN.reference_corpus_is_created();
    WHEN.buildStackExchangeQuestionSet_is_invoked();
    THEN.built_corpus_matches_reference();
  }

  @Test public void test_that_deserialized_answer_equals_original_object() throws IngestionException {
    GIVEN.answer_set_is_built();
    THEN.deserialized_answers_match_original_answers();
  }

  @Test public void test_that_stack_exchange_answers_can_be_created_from_questionids_posts_users_votes_postlinks_and_sitename()
      throws JAXBException {
    GIVEN.reference_answers_are_created();
    WHEN.buildAnswersForQuestion_is_invoked();
    THEN.builtAnswersForQuestion_matches_reference();
  }

  @Test public void test_that_deserialized_question_equals_original_object() throws IngestionException {
    GIVEN.question_set_is_built();
    THEN.deserialized_questions_match_original_questions();
  }

  @Test public void test_that_duplicate_threads_can_be_deserialized_correctly_from_txt_file()
      throws IngestionException {
    GIVEN.corpus_is_built();
    THEN.deserialiezd_duplicate_threads_should_match_original_duplicate_threads();
  }

  private void corpus_is_built() throws IngestionException {
    String dupCorpusPath = RetrieveAndRankIndexerTest.class.getResource("/dupCorpus").getPath();

    Properties corpusConfig = new Properties();
    corpusConfig.put(ConfigurationConstants.CORPUS_XML_DIR, dupCorpusPath);
    corpusConfig.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "DupPosts.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "DupVotes.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "DupPostLinks.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "DupUsers.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    corpusConfig.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(dup)");
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(dup)");
    corpusConfig.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(dup)");
    corpusConfig.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    dupCorpusBuilder = new CorpusBuilder();
    dupCorpusBuilder.initialize(corpusConfig);
    dupCorpusBuilder.buildCorpus();
  }

  private void build_corpus_from_filename_is_invoked() throws IngestionException {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);
    sampleCorpusBuilder.buildCorpus(INPUT_POSTS_FILE_NAME, INPUT_USERS_FILE_NAME, INPUT_VOTES_FILE_NAME,
        INPUT_LINKS_FILE_NAME, SITE_NAME);
    builtCorpus = sampleCorpusBuilder.getUniqueThreadSetFromBinFiles();
  }

  private void built_corpus_matches_reference() {
    for (StackExchangeThread thread : referenceCorpus)
      assertTrue(builtCorpus.contains(thread));
    for (StackExchangeThread thread : builtCorpus)
      assertTrue(referenceCorpus.contains(thread));
  }

  private void build_corpus_is_invoked() throws IngestionException {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);
    sampleCorpusBuilder.buildCorpus();
    builtCorpus = sampleCorpusBuilder.getUniqueThreadSetFromBinFiles();
  }

  private void reference_corpus_is_created() {
    if (referenceCorpus == null) {
      StackExchangeAnswer answer1 =
          new StackExchangeAnswer(INPUT_POSTS.getPosts().get(1), INPUT_USERS.getUserById(9214), SITE_NAME);
      StackExchangeAnswer answer2 =
          new StackExchangeAnswer(INPUT_POSTS.getPosts().get(2), INPUT_USERS.getUserById(9220), SITE_NAME);
      Set<StackExchangeAnswer> answerSet = new HashSet<>();
      answerSet.add(answer1);
      answerSet.add(answer2);

      StackExchangeThread question1 =
          new StackExchangeThread(INPUT_POSTS.getPosts().get(0), INPUT_USERS.getUserById(9216), answerSet, SITE_NAME,
              INPUT_VOTES.getVotesByPostId(6715), INPUT_POSTLINKS.getLinksByPostId(6715));

      // Corner Test cases for questions with no answers
      StackExchangeThread question2 =
          new StackExchangeThread(INPUT_POSTS.getPosts().get(3), null, null, SITE_NAME, null, null);

      referenceCorpus = new HashSet<>();
      referenceCorpus.add(question1);
      referenceCorpus.add(question2);
    }
  }

  private void buildStackExchangeQuestionSet_is_invoked() throws IngestionException {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);
    sampleCorpusBuilder.buildCorpus(INPUT_POSTS, INPUT_USERS, INPUT_VOTES, INPUT_POSTLINKS, SITE_NAME);

    builtCorpus = sampleCorpusBuilder.getUniqueThreadSetFromBinFiles();

  }

  private void reference_answers_are_created() throws JAXBException {
    Posts posts = CorpusBuilder.unmarshallFile(INPUT_POSTS_FILE, Posts.class);
    Users users = CorpusBuilder.unmarshallFile(INPUT_USERS_FILE, Users.class);

    StackExchangeAnswer answer1 = new StackExchangeAnswer(posts.getPosts().get(1), users.getUserById(9214), SITE_NAME);
    StackExchangeAnswer answer2 = new StackExchangeAnswer(posts.getPosts().get(2), users.getUserById(9220), SITE_NAME);
    referenceAnswers = new HashSet<>();
    referenceAnswers.add(answer1);
    referenceAnswers.add(answer2);
  }

  private void builtAnswersForQuestion_matches_reference() {
    assertTrue("Mismatch between reference answer corpus and built answer corpus",
        referenceAnswers.equals(builtAnswers));
  }

  private void buildAnswersForQuestion_is_invoked() {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);
    builtAnswers = sampleCorpusBuilder.buildAnswersForQuestion(INPUT_QUESTION_ID, INPUT_POSTS, INPUT_USERS, INPUT_VOTES,
        INPUT_POSTLINKS, SITE_NAME);

  }

  private void deserialized_answers_match_original_answers() throws IngestionException {
    for (StackExchangeAnswer answer : ANSWER_SET) {
      byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(answer);
      StackExchangeAnswer deserializedAnswer =
          (StackExchangeAnswer) StackExchangeThreadSerializer.deserializeObjFromBinArr(binCode);
      assertTrue(answer.equals(deserializedAnswer));
      assertEquals(answer.hashCode(), deserializedAnswer.hashCode());
    }
  }

  private void answer_set_is_built() {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);

    ANSWER_SET = new HashSet<StackExchangeAnswer>();
    for (Post questionPost : INPUT_POSTS.getPostsByParentId(null))
      ANSWER_SET.addAll(sampleCorpusBuilder.buildAnswersForQuestion(questionPost.getId(), INPUT_POSTS, INPUT_USERS,
          INPUT_VOTES, INPUT_POSTLINKS, SITE_NAME));
  }

  private void question_set_is_built() throws IngestionException {
    String sampleCorpusDir = CorpusBuilderTest.class.getResource("/sampleCorpus").getPath();

    Properties properties = new Properties();
    properties.put(ConfigurationConstants.CORPUS_XML_DIR, sampleCorpusDir);
    properties.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "SamplePosts.xml");
    properties.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "SampleVotes.xml");
    properties.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "SamplePostLinks.xml");
    properties.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "SampleUsers.xml");
    properties.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    properties.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads(sample)");
    properties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads(sample)");
    properties.put(ConfigurationConstants.ANALYZER, PrimarySearchConstants.ENGLISH_ANALYZER);

    sampleCorpusBuilder = new CorpusBuilder();
    sampleCorpusBuilder.initialize(properties);
    sampleCorpusBuilder.buildCorpus();
    sampleCorpusBuilder.buildCorpus(INPUT_POSTS, INPUT_USERS, INPUT_VOTES, INPUT_POSTLINKS, SITE_NAME);
    QUESTION_SET = sampleCorpusBuilder.getUniqueThreadSetFromBinFiles();
  }

  private void deserialized_questions_match_original_questions() throws IngestionException {
    for (StackExchangeThread question : QUESTION_SET) {
      StackExchangeThread deserializedQuestion = serialize_and_deserialize_question_with_byte_array(question);
      assertTrue(question.equals(deserializedQuestion));
      assertEquals(question.hashCode(), deserializedQuestion.hashCode());

      deserializedQuestion = serialize_and_deserialize_question_with_binary_file(question);
      assertTrue(question.equals(deserializedQuestion));
      assertEquals(question.hashCode(), deserializedQuestion.hashCode());
    }
  }

  private StackExchangeThread serialize_and_deserialize_question_with_byte_array(StackExchangeThread question)
      throws IngestionException {
    byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(question);
    return StackExchangeThreadSerializer.deserializeThreadFromBinArr(binCode);
  }

  private StackExchangeThread serialize_and_deserialize_question_with_binary_file(StackExchangeThread question)
      throws IngestionException {
    String binFilePath = StackExchangeThreadSerializer.serializeThreadToBinFile(question,
        testOutputFolder.getRoot().getAbsolutePath() + File.separator + "testSerFiles/");
    return StackExchangeThreadSerializer.deserializeThreadFromBinFile(binFilePath,
        testOutputFolder.getRoot().getAbsolutePath() + File.separator + "testSerFiles/");
  }

  private void deserialiezd_duplicate_threads_should_match_original_duplicate_threads() throws IngestionException {

    String csvFilePath = dupCorpusBuilder.getDupThreadDirPath() + StackExchangeConstants.DUP_THREAD_TSV_FILE_NAME
        + StackExchangeConstants.DUP_THREAD_TSV_FILE_EXTENSION;
    File csvData = new File(csvFilePath);

    CSVParser parser;
    List<CSVRecord> records;
    try {
      parser = CSVParser.parse(csvData, Charset.defaultCharset(), CSVFormat.TDF.withHeader());
      records = parser.getRecords();
    } catch (IOException e) {
      throw new IngestionException(e);
    }

    Set<StackExchangeThread> dupThreadSet = dupCorpusBuilder.getDupThreadSetFromBinFiles();
    for (StackExchangeThread thread : dupThreadSet) {
      String binfileName =
          dupCorpusBuilder.getDupThreadDirPath() + thread.getId() + StackExchangeConstants.BIN_FILE_SUFFIX;
      CSVRecord matchRecord = null;
      for (CSVRecord record : records)
        if (Integer.parseInt(record.get(0)) == thread.getId()) {
          matchRecord = record;
          break;
        }
      assertTrue(matchRecord != null);
      // TODO haven't check the originId yet since it requires the new
      // method to get origin id from
      String deserTitle = matchRecord.get(1), deserBody = matchRecord.get(2), deserFileName = matchRecord.get(4),
          deserTags = matchRecord.get(5);
      assertEquals(deserTitle, thread.getQuestion().getTitle());
      assertEquals(deserBody, thread.getQuestion().getBody());
      assertEquals(deserFileName, binfileName);
      assertEquals(deserTags, thread.getConcatenatedTagsText());
    }
  }

}
