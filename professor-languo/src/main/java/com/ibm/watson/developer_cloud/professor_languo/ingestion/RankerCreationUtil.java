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
package com.ibm.watson.developer_cloud.professor_languo.ingestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.endpoints.AnswerFileBody;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcherConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.BaseEgaMetaDataAnswerScorer;

/**
 * Class for utilities related to creating and training the Retrieve and Rank ranker.
 * 
 */
public class RankerCreationUtil {

  private final static Logger logger = LogManager.getLogger(RankerCreationUtil.class.getCanonicalName());

  /**
   * Retrieve a {@link CandidateAnswer} with its {@code threadPostId} by querying the /select
   * endpoint with query THREAD_POST_ID:x
   * 
   * @param searcher An initialized {@link RetrieveAndRankSearcher}
   * @param threadPostId The THREAD_POST_ID of the answer thread
   * @return
   * @throws IOException
   * @throws IngestionException
   */
  public static CandidateAnswer getCandidateAnswerById(RetrieveAndRankSearcher searcher, String threadPostId)
      throws IOException, IngestionException {

    CandidateAnswer answer = null;
    try {
      SolrQuery featureSolrQuery = new SolrQuery(RetrieveAndRankSearcherConstants.ID_FIELD + ":" + threadPostId);

      // specify the request handler for the feature query
      featureSolrQuery.setRequestHandler(RetrieveAndRankSearcherConstants.SELECT_REQUEST_HANDLER);

      // We expect only one response since THREAD_POST_ID is a unique key
      if (featureSolrQuery.size() != 1) {
        throw new IngestionException(threadPostId);
      }
      featureSolrQuery.setRows(1);
      final QueryRequest featureRequest = new QueryRequest(featureSolrQuery);

      QueryResponse featureResponse = null;

      featureResponse = searcher.processSolrRequest(featureRequest);
      for (SolrDocument doc : featureResponse.getResults()) {
        byte[] bin = (byte[]) doc.getFieldValue(IndexDocumentFieldName.SERIALIZED_THREAD.toString());
        answer = StackExchangeThreadSerializer.deserializeThreadFromBinArr(bin);
      }
    } catch (IOException | SolrServerException | InterruptedException e) {
      logger.error(e.getMessage());
    }
    return answer;
  }

  /**
   * Trains the ranker using the trainingdata.csv
   * 
   * @param ranker_url URL associated with the ranker Ex.)
   *        "https://gateway.watsonplatform.net/retrieve-and-rank/api/v1/rankers"
   * @param rankerName The name of the ranker, to be sent as metadata
   * @param client {@link HttpClient} to send the request to
   * @param training_file Path to the trainingdata.csv
   * @return JSON of the result: { "name": "example-ranker", "url":
   *         "https://gateway.watsonplatform.net/retrieve-and-rank/api/v1/rankers/6C76AF-ranker-43",
   *         "ranker_id": "6C76AF-ranker-43", "created": "2015-09-21T18:01:57.393Z", "status":
   *         "Training", "status_description":
   *         "The ranker instance is in its training phase, not yet ready to accept requests" }
   * @throws IOException
   */
  public static String trainRanker(String ranker_url, String rankerName, HttpClient client, StringBuffer training_data)
      throws IOException {
    // Create a POST request
    HttpPost post = new HttpPost(ranker_url);
    MultipartEntityBuilder postParams = MultipartEntityBuilder.create();

    // Add data
    String metadata = "\"" + rankerName + "\"";
    StringBody metadataBody = new StringBody(metadata, ContentType.TEXT_PLAIN);
    postParams.addPart(RetrieveAndRankSearcherConstants.TRAINING_DATA_LABEL,
        new AnswerFileBody(training_data.toString()));
    postParams.addPart(RetrieveAndRankSearcherConstants.TRAINING_METADATA_LABEL, metadataBody);
    post.setEntity(postParams.build());

    // Obtain and parse response
    HttpResponse response = client.execute(post);
    String result = getHttpResultString(response);
    return result;
  }

  /**
   * Creates an HttpClient
   * 
   * @param auth {@link AuthScope} object specifying the scope of this client
   * @param creds {@link Credentials} for the client
   * @return {@link HttpClient} client to make http requests
   */
  public static CloseableHttpClient createHttpClient(AuthScope auth, Credentials creds) {
    CredentialsProvider provider = new BasicCredentialsProvider();
    provider.setCredentials(auth, creds);
    CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    return client;
  }

  /**
   * 
   * @param uri
   * @param username
   * @param password
   * @return
   */
  public static HttpClient createHttpClient(String uri, String username, String password) {
    final URI scopeUri = URI.create(uri);

    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(scopeUri.getHost(), scopeUri.getPort()),
        new UsernamePasswordCredentials(username, password));

    final HttpClientBuilder builder = HttpClientBuilder.create().setMaxConnTotal(128).setMaxConnPerRoute(32)
        .setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).build());
    builder.setDefaultCredentialsProvider(credentialsProvider);

    return builder.build();
  }

  /**
   * Converts an HttpResponse object to a simple String
   * 
   * @param response the {@link HttpResponse} object
   * @return String of the content
   * @throws UnsupportedOperationException
   * @throws IOException
   */
  public static String getHttpResultString(HttpResponse response) throws UnsupportedOperationException, IOException {
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String l = "";
    while ((l = rd.readLine()) != null) {
      result.append(l);
      result.append("\n");
    }
    rd.close();
    return result.toString().trim();
  }

  /**
   * Send a post request to the server to rank answers in the csvAnswerData
   * 
   * @param client Authorized {@link HttpClient}
   * @param ranker_url URL of the ranker to hit. Ex.)
   *        https://gateway.watsonplatform.net/retrieve-and-rank/api/v1/ rankers/{ranker_id}/rank
   * @param csvAnswerData A string with the answer data in csv form
   * @return JSONObject of the response from the server
   * @throws ClientProtocolException
   * @throws IOException
   * @throws HttpException
   * @throws JSONException
   */
  public static JSONObject rankAnswers(CloseableHttpClient client, String ranker_url, String csvAnswerData)
      throws ClientProtocolException, IOException, HttpException, JSONException {

    // If there is no csv data return an empty array
    if (csvAnswerData.trim().equals("")) {
      return new JSONObject("{\"code\":200 , \"answers\" : []}");
    }
    // Create post request to rank answers
    HttpPost post = new HttpPost(ranker_url);
    MultipartEntityBuilder postParams = MultipartEntityBuilder.create();

    // Fill in post request data
    postParams.addPart(RetrieveAndRankConstants.ANSWER_DATA, new AnswerFileBody(csvAnswerData));
    post.setEntity(postParams.build());

    // Send post request and get resulting response
    HttpResponse response = client.execute(post);
    String responseString = RankerCreationUtil.getHttpResultString(response);
    JSONObject responseJSON = null;
    try {
      responseJSON = (JSONObject) JSON.parse(responseString);
    } catch (NullPointerException | JSONException e) {
      logger.error(e.getMessage());
    }
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new HttpException(responseString + ":" + post);
    }
    return responseJSON;
  }

  /**
   * Creates a csv string of answer data from a collection of answers by reading the answer ids and
   * feature vectors
   * 
   * @param answers the collection of CandidatateAnswers to pull data from
   * @return
   */
  public static String getCsvAnswerData(Collection<CandidateAnswer> answers, BaseEgaMetaDataAnswerScorer scorer) {

    // Initialize the csv string used to send the answer data for ranking
    StringBuilder csvStringHeader = new StringBuilder();
    StringBuilder csvString = new StringBuilder();

    // Add the answer data to to the csv string
    boolean addHeader = true;
    for (CandidateAnswer answer : answers) {

      // score the answer to add our own features
      if (scorer != null) {
        // answers come from real-time user, not testing phase of the
        // pipeline
        answer = scorer.scoreCandidateAnswer(null, answer);
      }

      // Add header
      if (addHeader) {
        csvStringHeader.append(RetrieveAndRankConstants.ANSWER_ID_HEADER + ",");
      }
      csvString.append(answer.getAnswerLabel());
      csvString.append(",");

      // Build the csvString by adding the feature vectors
      Set<Entry<String, Double>> fvp = answer.getFeatureValuePairs();
      Iterator<Entry<String, Double>> it = fvp.iterator();
      while (it.hasNext()) {
        Entry<String, Double> entry = (Entry<String, Double>) it.next();
        // ignore the added rank and score fields as they are not needed
        // by ranker
        if (!(entry.getKey().equals(PipelineConstants.FEATURE_SEARCH_RANK)
            || entry.getKey().equals(PipelineConstants.FEATURE_SEARCH_SCORE))) {
          if (addHeader) {
            csvStringHeader.append(entry.getKey());
            csvStringHeader.append(",");
          }
          csvString.append(entry.getValue());
          csvString.append(",");
        }
      }

      // delete last comma of the header then begin a new line
      if (addHeader) {
        csvStringHeader.delete(csvStringHeader.length() - 1, csvStringHeader.length());
        csvStringHeader.append("\n");
      }

      // delete trailing comma then begin a new line
      csvString.delete(csvString.length() - 1, csvString.length());
      csvString.append("\n");
      addHeader = false;
    }

    return csvStringHeader.append(csvString).toString();
  }

  /**
   * Returns the status of the specified ranker
   * 
   * @param client {@link CloseableHttpClient} object with credentials
   * @param ranker_url URL to the ranker. Ex.)
   *        "https://gateway.watsonplatform.net/retrieve-and-rank/api/v1/ rankers/{ranker_id}
   * @return status. Ex.) "Available", "Training", "Failed"
   * @throws IOException
   */
  public String getRankerStatus(CloseableHttpClient client, String ranker_url) throws IOException {

    String status = null;
    JSONObject res;

    try {
      HttpGet httpget = new HttpGet(ranker_url);
      CloseableHttpResponse response = client.execute(httpget);

      try {

        String result = EntityUtils.toString(response.getEntity());
        res = new JSONObject(result);
        status = res.getString("status");

      } catch (JSONException e) {
        throw new RuntimeException("JSON Exception!", e);
      }

      finally {
        response.close();
      }
    }

    finally {
      client.close();
    }

    return status;
  }
}
