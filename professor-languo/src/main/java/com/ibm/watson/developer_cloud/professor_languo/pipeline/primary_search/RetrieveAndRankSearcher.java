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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.RankerCreationUtil;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;

/**
 * A searcher class used to send search queries to the solr cluster
 */
public class RetrieveAndRankSearcher implements Searcher {

  private static final Logger log = LogManager.getLogger(RetrieveAndRankSearcher.class.getName());;

  private String collection_name;
  private String request_handler = RetrieveAndRankSearcherConstants.FCSELECT_REQUEST_HANDLER;
  int numberOfAnswers;
  private int query_retry_limit = 5;

  HttpSolrClient solrClient;

  /**
   * Creates search service searcher with FCSSELECT request handler as default
   */
  public RetrieveAndRankSearcher() {}

  /**
   * Creates search service searcher with request handler
   * 
   * @param requestHandler - Use FCSELECT for free queries or SELECT for normal queries
   */
  public RetrieveAndRankSearcher(String requestHandler) {
    setRequestHandler(requestHandler);
  }

  /**
   * Set the request handler
   * 
   * @param requestHandler
   */
  public void setRequestHandler(String requestHandler) {
    request_handler = requestHandler;
  }

  /**
   * Initialized the searcher with configurations from the properties
   * 
   * @param properties - the properties object to use
   * @throws SearchExcetption - an invalid URL in the properties file throws a SearchException
   */
  @Override public void initialize(Properties properties) throws SearchException {

    // Read the bluemix properties
    query_retry_limit = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.QUERY_RETRY_LIMIT));

    // Read the bluemix properties
    String username = properties.getProperty(RetrieveAndRankConstants.USERNAME);
    String password = properties.getProperty(RetrieveAndRankConstants.PASSWORD);
    collection_name = properties.getProperty(RetrieveAndRankConstants.COLLECTION);
    String solr_cluster_id = properties.getProperty(RetrieveAndRankConstants.SOLR_CLUSTER_ID);

    // Set the number of rows to ask for
    numberOfAnswers = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.CANDIDATE_ANSWER_NUM,
        RetrieveAndRankConstants.DEFUALT_CANDIDATE_ANSWER_NUM));

    final RetrieveAndRank service = new RetrieveAndRank();
    service.setUsernameAndPassword(username, password);
    final String uri = service.getSolrUrl(solr_cluster_id);
    solrClient = new HttpSolrClient(uri, RankerCreationUtil.createHttpClient(uri, username, password));
  }

  /**
   * Perform a search query on the cluster
   * 
   * @param query - the query to send to the cluster
   * @return answers - A list of CandidateAnswers built from docs in the search results
   * @throws SearchException
   */
  public Collection<CandidateAnswer> performSearch(Query query) throws SearchException {
    return performSearch(query.toString(), numberOfAnswers);
  }

  public Collection<CandidateAnswer> performSearch(String query) throws SearchException {
    return performSearch(query, numberOfAnswers);
  }

  public Collection<CandidateAnswer> performSearch(String query, int numAns) throws SearchException {

    SolrQuery featureSolrQuery = new SolrQuery(query);

    // Specify the request handler for the feature query
    featureSolrQuery.setRequestHandler(request_handler);
    // Specify parameters for the response
    featureSolrQuery.setParam(RetrieveAndRankSearcherConstants.FIELD_LIST_PARAM,
        RetrieveAndRankSearcherConstants.ID_FIELD + "," + RetrieveAndRankSearcherConstants.FEATURE_VECTOR_FIELD + ","
            + IndexDocumentFieldName.SERIALIZED_THREAD.toString());

    featureSolrQuery.setRows(numAns);

    // Make the request
    final QueryRequest featureRequest = new QueryRequest(featureSolrQuery);
    QueryResponse featureResponse = null;
    try {
      featureResponse = processSolrRequest(featureRequest);
    } catch (IOException | SolrServerException | InterruptedException e) {
      log.error(e.toString(), e);
      throw new SearchException(e);
    }
    return responseToCollection(featureResponse);
  }

  /**
   * Sends a given query request to the solr cluster server and returns the server response
   * containing the documents that match the query in ranked order. a maximum of MAX_QUERY_ATTEMPTS
   * will be made to deliver the query to the solr server.
   * 
   * @param request - the Solr query
   * @return response - QueryResponse object with a getResults method to get the matched docs
   * @throws IOException
   * @throws SolrServerException
   * @throws InterruptedException
   */
  public QueryResponse processSolrRequest(final QueryRequest request)
      throws IOException, SolrServerException, InterruptedException {
    int currentAttempt = 0;
    QueryResponse response;
    while (true) {
      try {
        currentAttempt++;
        response = request.process(solrClient, collection_name);
        break;
      } catch (final Exception e) {
        log.trace(MessageFormat.format(Messages.getString("RetrieveAndRank.QUERY_RETRY_FAILED"), currentAttempt)); //$NON-NLS-1$
        if (currentAttempt < query_retry_limit) {
          log.trace(e);
          log.trace(MessageFormat.format(Messages.getString("RetrieveAndRank.QUERY_RETRY"), currentAttempt)); //$NON-NLS-1$
          // wait for 1 second before retrying
          Thread.sleep(1000);
        } else {
          log.error(e);
          throw e;
        }
      }
    }
    return response;
  }

  /**
   * Convert a {@link QueryResponse} to a {@link Collection} of {@link CandidateAnswer}s The
   * {@code response} must be from a {@link SolrQuery} that expects a list of answers as a response
   * 
   * @param response
   * @return
   * @throws SearchException
   */
  public Collection<CandidateAnswer> responseToCollection(QueryResponse response) throws SearchException {
    // Create a collection of answers to store the results of the query
    Collection<CandidateAnswer> answers = new HashSet<CandidateAnswer>();

    // Collect feature vectors and construct a CandidateAnswer
    for (SolrDocument doc : response.getResults()) {
      // read the binary SERIALIZED_THREAD field from the docs in the
      // search results
      Object bin = doc.getFieldValue(IndexDocumentFieldName.SERIALIZED_THREAD.toString());

      CandidateAnswer candidateAnswer;
      try {
        // create an answer from the deserialized thread.
        candidateAnswer = StackExchangeThreadSerializer.deserializeThreadFromBinArr((byte[]) bin);

        if (request_handler.equals(RetrieveAndRankSearcherConstants.FCSELECT_REQUEST_HANDLER)) {
          // If response is for querying
          String[] features = doc.getFieldValue(RetrieveAndRankSearcherConstants.FEATURE_VECTOR_FIELD).toString().trim()
              .split(RetrieveAndRankSearcherConstants.FEATURE_VECTOR_DELIM);
          for (int i = 0; i < features.length; i++) {
            candidateAnswer.setFeatureValue(RetrieveAndRankSearcherConstants.FEATURE_HEADER + i,
                Double.parseDouble(features[i]));
          }
        }

        answers.add(candidateAnswer);
      } catch (IngestionException e) {
        throw new SearchException(e);
      }
    }

    return answers;
  }
}
