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

package com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.IndexingStats;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.LuceneDocumentMapper;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.RetrieveAndRankIndexer;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.CorpusBuilder;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexStatFieldName;

/**
 * Unit tests for the rank and retrieve indexer
 */
public class RetrieveAndRankIndexerTest {
  private RetrieveAndRankIndexerTest GIVEN = this, WHEN = this, AND = this, THEN = this;
  private String uniqThreadDirPath = null;
  private Set<StackExchangeThread> indexdCorpus = null;
  private IndexingStats indexingStats = null;
  private final static RetrieveAndRankIndexer indexer = new RetrieveAndRankIndexer();;
  private static CorpusBuilder corpusBuilder = null;
  private Properties appProperties;
  private String COLLECTION_NAME = "collection";
  private List<SolrInputDocument> indexedRecords = new ArrayList<>();

  /**
   * Initialize the indexer with appProperties configuration
   * 
   * @throws IngestionException
   */
  public void test_initialize() throws IngestionException {
    indexer.initialize(appProperties);
  }

  /**
   * Mock a solr client that mocks indexed documents in the indexedRecords field
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @SuppressWarnings("unchecked") public void mock_solrClient() throws SolrServerException, IOException {
    indexer.solrClient = mock(HttpSolrClient.class);

    // mocks indexed documents in the indexedRecords field
    Answer<String> answer = new Answer<String>() {
      @Override public String answer(InvocationOnMock invocation) throws Throwable {
        List<SolrInputDocument> batch = (List<SolrInputDocument>) invocation.getArguments()[1];
        indexedRecords.addAll(batch);
        return null;
      }
    };
    when(indexer.solrClient.add(eq(COLLECTION_NAME), any(List.class))).thenAnswer(answer);
  }

  /**
   * Mock a faulty SolrClient that throw a SolrServer Exception
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @SuppressWarnings("unchecked") public void mock_bad_solrClient() throws SolrServerException, IOException {
    indexer.solrClient = mock(HttpSolrClient.class);
    when(indexer.solrClient.add(eq(COLLECTION_NAME), any(List.class))).thenThrow(SolrServerException.class);
  }

  /**
   * Setup valid Solr connection properties
   */
  @Before public void setup_properties() {
    appProperties = new Properties();
    appProperties.put(RetrieveAndRankConstants.USERNAME, "username");
    appProperties.put(RetrieveAndRankConstants.PASSWORD, "password");
    appProperties.put(RetrieveAndRankConstants.RNR_ENDPOINT, "endpoint");
    appProperties.put(RetrieveAndRankConstants.COLLECTION, COLLECTION_NAME);
    appProperties.put(RetrieveAndRankConstants.SOLR_CLUSTER_ID, "someid");
    appProperties.put(RetrieveAndRankConstants.CSV_FILE, "");
  }

  /**
   * Test failure on attempting to connect to a wrong endpoint
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_failure_on_bad_properties() throws SolrServerException, IOException {
    Properties appProperties = new Properties();
    appProperties.put(RetrieveAndRankConstants.USERNAME, "25ed9727-0907-4e24-becd-c2c9e35fa462");
    appProperties.put(RetrieveAndRankConstants.PASSWORD, "z3eBOZqPcqV2");
    appProperties.put(RetrieveAndRankConstants.RNR_ENDPOINT, "https://badendpoint.com");
    appProperties.put(RetrieveAndRankConstants.COLLECTION, "rnr-collection");
    appProperties.put(RetrieveAndRankConstants.SOLR_CLUSTER_ID, "sced6cdef4_28a2_42a5_a3a8_a6c27f8a9bfc");
    try {
      mock_solrClient();
      test_initialize();
    } catch (IngestionException e) {
      assertTrue(e.getMessage().contains("failed to connect to solr cluster:"));
    }
  }

  /**
   * Test that a properties file with missing fields fails as expected
   * 
   * @throws IngestionException
   */
  @Test(expected = IllegalArgumentException.class) public void test_faliure_on_empty_properties()
      throws IngestionException {
    // an empty properties file should throw an exception
    appProperties = new Properties();
    test_initialize();
  }

  /**
   * Test that SolrServer exceptions thrown by the SolrClient are handled correctly
   * 
   * @throws IngestionException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test(expected = IngestionException.class) public void test_server_throw_exeption()
      throws IngestionException, SolrServerException, IOException {
    GIVEN.corpus_is_built();
    AND.rnr_indexer_is_created();
    AND.mock_bad_solrClient();
    AND.documents_are_indexed();
    THEN.compare_indexed_records_to_corpus();
  }

  /**
   * Test that RnR doesn't write to non existent directories
   * 
   * @throws IngestionException
   */
  @Test(expected = NullPointerException.class) public void bad_index_corpus_dir() throws IngestionException {
    RetrieveAndRankIndexer rnr = new RetrieveAndRankIndexer();
    rnr.initialize(appProperties);
    String filePathString = new Random().nextInt(9999999) + "";
    // generate random string
    File f = new File(filePathString);
    // check that file path doesn't exist
    while (f.exists() || f.isDirectory()) {
      filePathString = new Random() + "";
      f = new File(filePathString);
    }
    // test the non existent file path for null pointer exception
    rnr.indexCorpus(filePathString);
  }

  @Rule public TemporaryFolder testOutputFolder = new TemporaryFolder();

  /**
   * Test that the correct number of documents from the corpus are indexed
   * 
   * @throws IngestionException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_that_indexes_correct_number_of_documents()
      throws IngestionException, SolrServerException, IOException {
    GIVEN.corpus_is_built();
    WHEN.rnr_indexer_is_created();
    THEN.mock_solrClient();
    THEN.documents_are_indexed();
    THEN.index_stats_contains_correct_number_of_indexed_documents();
  }

  /**
   * Test that all documents from the corpus are indexed correctly
   * 
   * @throws IngestionException
   * @throws SearchException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_that_all_records_are_indexed()
      throws IngestionException, SearchException, SolrServerException, IOException {
    GIVEN.corpus_is_built();
    AND.rnr_indexer_is_created();
    AND.mock_solrClient();
    AND.documents_are_indexed();
    THEN.compare_indexed_records_to_corpus();
  }

  /**
   * Check that the index stats file contains the correct number of indexed documents
   * 
   * @throws IngestionException
   */
  private void index_stats_contains_correct_number_of_indexed_documents() throws IngestionException {
    int expectedIndexDocNum = (new File(uniqThreadDirPath)).listFiles().length;

    int indexDocNum = (int) indexingStats.getStatistic(IndexStatFieldName.INDEX_DOC_NUM.toString());
    assertTrue(expectedIndexDocNum == indexDocNum);
  }

  /**
   * Initialize the indexer with configurations for the corpus
   * 
   * @throws IngestionException
   * @throws SolrServerException
   * @throws IOException
   */
  private void rnr_indexer_is_created() throws IngestionException, SolrServerException, IOException {
    Properties indexConfig = new Properties();
    indexConfig.put(ConfigurationConstants.INDEX_DIR_TYPE, ConfigurationConstants.IndexDirTypes.FS.toString());
    indexConfig.put(ConfigurationConstants.INDEX_DIR, "indexDir");
    indexConfig.put(ConfigurationConstants.INDEX_STAT_PATH, "indexDir/indexStat.txt");

    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    indexConfig.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    indexConfig.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads");
    indexConfig.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads");
    indexConfig.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads");

    indexConfig.put(RetrieveAndRankConstants.USERNAME, "some_username");
    indexConfig.put(RetrieveAndRankConstants.PASSWORD, "some_password");
    indexConfig.put(RetrieveAndRankConstants.RNR_ENDPOINT, "some_endpoint");
    indexConfig.put(RetrieveAndRankConstants.COLLECTION, COLLECTION_NAME);
    indexConfig.put(RetrieveAndRankConstants.SOLR_CLUSTER_ID, "some_cluster_id");

    indexer.initialize(indexConfig);
  }

  /**
   * Index the Corpus using the RnR indexer
   * 
   * @throws IngestionException
   * @throws SolrServerException
   * @throws IOException
   */
  @SuppressWarnings("unchecked") private void documents_are_indexed()
      throws IngestionException, SolrServerException, IOException {
    indexingStats = indexer.indexCorpus(corpusBuilder.getUniqueThreadDirPath());
    verify(indexer.solrClient).add(eq(COLLECTION_NAME), any(List.class));
  }

  /**
   * Build corpus used for the tests
   * 
   * @throws IngestionException
   */
  private void corpus_is_built() throws IngestionException {
    String dupCorpusPath = RetrieveAndRankIndexerTest.class.getResource("/dupCorpus").getPath();

    Properties corpusConfig = new Properties();
    corpusConfig.put(ConfigurationConstants.CORPUS_XML_DIR, dupCorpusPath);
    corpusConfig.put(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME, "DupPosts.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME, "DupVotes.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME, "DupPostLinks.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_USERS_XML_FILENAME, "DupUsers.xml");
    corpusConfig.put(ConfigurationConstants.CORPUS_SITE_NAME, "robotics.stackexchange.com");

    // temporarily use the a test output folder
    final String base_dir = testOutputFolder.getRoot().getAbsolutePath();
    corpusConfig.put(ConfigurationConstants.INGESTION_BASE_DIR, base_dir);
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads");
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads");
    corpusConfig.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads");

    corpusConfig.put(ConfigurationConstants.ANALYZER, "ENGLISH_ANALYZER");

    corpusBuilder = new CorpusBuilder();
    corpusBuilder.initialize(corpusConfig);
    uniqThreadDirPath = corpusBuilder.buildCorpus();
  }

  /**
   * check if the deserialized candidate answers are exactly the same as the candidate answers we
   * expected. Since the query is "question Title: what is right", the expected candidate answers
   * should be the entire corpus.
   * 
   * @throws IngestionException
   */
  private void compare_indexed_records_to_corpus() throws IngestionException {
    indexdCorpus = corpusBuilder.getUniqueThreadSetFromBinFiles();

    // Check that the size of the corpus is the same as the size of the
    // indexed documents
    assertTrue("Wrong number of documents indexed", indexedRecords.size() == indexdCorpus.size());

    // Check that the indexed document in the corpus is in the index
    File serFile = new File(corpusBuilder.getUniqueThreadDirPath()).listFiles()[0];
    StackExchangeThread thread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(serFile.getPath());

    final Document luceneDoc = new LuceneDocumentMapper().createDocument(thread);
    SolrInputDocument recordDoc = indexedRecords.get(0);

    for (IndexableField field : luceneDoc.getFields()) {
      BytesRef bin = luceneDoc.getBinaryValue(field.name());

      // Check that indexed fields (title and id) are indexed correctly
      if (field.name().equals(IndexDocumentFieldName.THREAD_TITLE.toString())
          || field.name().equals(IndexDocumentFieldName.THREAD_POST_ID.toString())) {

        String value = luceneDoc.get(field.name());
        assertEquals(value, recordDoc.getFieldValue(field.name()).toString());
      }

      // Check that indexed serialized field is indexed correctly
      if (bin != null) {
        BytesRef recordbin = new BytesRef((byte[]) recordDoc.getFieldValue(field.name()));
        assertEquals(bin, recordbin);
      }

    }
  }

}
