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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.LuceneDocumentMapper;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.RetrieveAndRankIndexerTest;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.CorpusBuilder;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;

public class RetrieveAndRankSearcherTest {

  RetrieveAndRankSearcherTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private static CorpusBuilder corpusBuilder = null;
  private Properties appPropeties;
  private Query query;
  private final NamedList<Object> response = new NamedList<>();
  RetrieveAndRankSearcher searcher;
  private Collection<CandidateAnswer> searchResponse;

  /**
   * Test that a simple blank query runs successfully with an empty response
   * 
   * @throws SearchException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_empty_query_and_response() throws SearchException, SolrServerException, IOException {
    GIVEN.searcher_is_initialized();
    // test empty query and response
    AND.create_empty_query();
    AND.set_empty_repsonse();
    AND.mock_solr_client_with_response();
    WHEN.perform_search();
    THEN.verify_empty_response();
  }

  /**
   * Test a simple non empty query and a response
   * 
   * @throws SearchException
   * @throws SolrServerException
   * @throws IOException
   * @throws IngestionException
   */
  @Test public void test_query_and_response()
      throws SearchException, SolrServerException, IOException, IngestionException {
    // test empty query and response
    GIVEN.searcher_is_initialized();
    THEN.create_query("a query");
    AND.set_non_empty_repsonse();
    AND.mock_solr_client_with_response();
    WHEN.perform_search();
    THEN.verify_response();
  }

  @Rule public TemporaryFolder testOutputFolder = new TemporaryFolder();

  /**
   * Create a query from a string and set it to the query field
   * 
   * @param query - A string for the query
   */
  private void create_query(final String query) {
    this.query = new Query() {
      @Override public String toString(String arg0) {
        return query;
      }
    };
  }

  /**
   * Initialize a new searcher with appProperties configuration filled with connection parameters
   * 
   * @throws SearchException
   */
  public void searcher_is_initialized() throws SearchException {
    // setup valid solr connection properties
    appPropeties = new Properties();
    appPropeties.put(RetrieveAndRankConstants.USERNAME, "username");
    appPropeties.put(RetrieveAndRankConstants.PASSWORD, "password");
    appPropeties.put(RetrieveAndRankConstants.RNR_ENDPOINT, "endpoint");
    appPropeties.put(RetrieveAndRankConstants.COLLECTION, "rnr-collection");
    appPropeties.put(RetrieveAndRankConstants.SOLR_CLUSTER_ID, "someid");
    appPropeties.put(RetrieveAndRankConstants.CSV_FILE, "");
    appPropeties.put(RetrieveAndRankConstants.QUERY_RETRY_LIMIT, "1");
    searcher = new RetrieveAndRankSearcher();
    searcher.initialize(appPropeties);
  }

  /**
   * Initialize searcher with bad url
   * 
   * @throws SearchException
   */
  public void searcher_is_initialized_with_bad_url() throws SearchException {
    appPropeties = new Properties();
    appPropeties.put(RetrieveAndRankConstants.USERNAME, "username");
    appPropeties.put(RetrieveAndRankConstants.PASSWORD, "password");
    appPropeties.put(RetrieveAndRankConstants.RNR_ENDPOINT, "[bad] [url]");
    appPropeties.put(RetrieveAndRankConstants.COLLECTION, "rnr-collection");
    appPropeties.put(RetrieveAndRankConstants.SOLR_CLUSTER_ID, "someid");
    appPropeties.put(RetrieveAndRankConstants.CSV_FILE, "");
    appPropeties.put(RetrieveAndRankConstants.QUERY_RETRY_LIMIT, "1");
    searcher = new RetrieveAndRankSearcher();
    searcher.initialize(appPropeties);
  }

  /**
   * Create an empty query
   */
  private void create_empty_query() {
    create_query(null);
  }

  private void perform_search() throws SearchException {
    searchResponse = new HashSet<CandidateAnswer>();
    searchResponse = searcher.performSearch(query);
  }

  private void set_response(SolrDocumentList response) {
    this.response.clear();
    this.response.add("response", response);
  }

  private String get_unique_thread_path() throws IngestionException {
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
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, base_dir + "/duplicateThreads");
    corpusConfig.put(ConfigurationConstants.DUPLICATE_THREAD_DIR, base_dir + "/duplicateThreads");
    corpusConfig.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, base_dir + "/uniqueThreads");

    corpusConfig.put(ConfigurationConstants.ANALYZER, "ENGLISH_ANALYZER");

    corpusBuilder = new CorpusBuilder();
    corpusBuilder.initialize(corpusConfig);
    String uniqThreadDirPath = corpusBuilder.buildCorpus();
    return uniqThreadDirPath;
  }

  private void set_non_empty_repsonse() throws IngestionException {
    String uniqThreadDirPath = get_unique_thread_path();
    SolrDocumentList doclist = new SolrDocumentList();
    SolrDocument doc = new SolrDocument();
    File serFile = new File(uniqThreadDirPath).listFiles()[0];
    StackExchangeThread thread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(serFile.getPath());

    final Document luceneDoc = new LuceneDocumentMapper().createDocument(thread);
    BytesRef bin = luceneDoc.getBinaryValue(IndexDocumentFieldName.SERIALIZED_THREAD.toString());
    doc.addField(IndexDocumentFieldName.SERIALIZED_THREAD.toString(), bin.bytes);
    doc.addField("score", Integer.MAX_VALUE);
    doc.addField("featureVector", Double.MAX_VALUE);
    doclist.add(doc);
    set_response(doclist);
  }

  private void verify_empty_response() {
    assertEquals(searchResponse, new HashSet<CandidateAnswer>());
  }

  /**
   * Check that the query response is expected given the mocked client response
   * 
   * @throws IngestionException
   */
  private void verify_response() throws IngestionException {
    HashSet<CandidateAnswer> expectedResponse = new HashSet<CandidateAnswer>();
    for (SolrDocument doc : ((SolrDocumentList) response.get("response"))) {
      Object bin = doc.getFieldValue(IndexDocumentFieldName.SERIALIZED_THREAD.toString());
      StackExchangeThread candidateAnswer = StackExchangeThreadSerializer.deserializeThreadFromBinArr((byte[]) bin);
      expectedResponse.add(candidateAnswer);
    }
    assertEquals(searchResponse, expectedResponse);
  }

  private void set_empty_repsonse() {
    set_response(new SolrDocumentList());
  }

  private void mock_solr_client() throws SolrServerException, IOException {
    searcher.solrClient = mock(HttpSolrClient.class);
  }

  private void mock_solr_client_with_response() throws SolrServerException, IOException {
    mock_solr_client();
    String collection_name = (String) appPropeties.get(RetrieveAndRankConstants.COLLECTION);
    when(searcher.solrClient.request(isA(SolrRequest.class), eq(collection_name))).thenReturn(response);
  }
}
