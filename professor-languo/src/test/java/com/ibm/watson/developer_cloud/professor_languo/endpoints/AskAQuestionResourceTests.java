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

package com.ibm.watson.developer_cloud.professor_languo.endpoints;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;
import com.ibm.watson.developer_cloud.professor_languo.endpoints.AskAQuestionResource;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Post;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;

public class AskAQuestionResourceTests {

  AskAQuestionResource askAQuestionResource = new AskAQuestionResource();
  RetrieveAndRankSearcher searcher;
  JSONArray response;
  String questionText = "question text";
  String answerId = "42";
  AskAQuestionResourceTests WHEN = this, THEN = this;
  private JSONArray expectedResponse;
  private ArrayList<CandidateAnswer> answers;
  private int numOfAnswers;

  /**
   * Reset the properties and mocks befoer each test
   * 
   * @throws JsonParseException
   * @throws IllegalArgumentException
   * @throws IOException
   */
  @org.junit.Before public void initialize() throws JsonParseException, IllegalArgumentException, IOException {
    searcher = mock(RetrieveAndRankSearcher.class);
    response = null;
    askAQuestionResource = spy(new AskAQuestionResource());
    askAQuestionResource.searcher = null;
    Properties properties = new Properties();
    properties.load(this.getClass().getResourceAsStream("/app_config.properties"));
    IndexerAndSearcherFactory.loadStaticBluemixProperties(properties);
    numOfAnswers = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.CANDIDATE_ANSWER_NUM));

  }

  /**
   * Test what happens when a question is sent and a non-empty set of documents is returned
   * 
   * @throws Exception
   */
  @Test public void run_successful_qa() throws Exception {
    WHEN.mock_successful_askqa(questionText);
    THEN.run_post();
    THEN.verify_response();
  }

  /**
   * Test what happens when the searcher fails with an exception
   * 
   * @throws Exception
   */
  @Test(expected = SearchException.class) public void run_failed_search_qa() throws Exception {
    WHEN.mock_failed_search_askqa(questionText);
    THEN.run_post();
    THEN.verify_response();
  }

  /**
   * Test what happens when the response is an empty set of documents
   * 
   * @throws Exception
   */
  @Test public void run_empty_search_qa() throws Exception {
    WHEN.mock_empty_search_askqa(questionText);
    THEN.run_post();
    THEN.verify_empty_response();
  }

  /***
   * Verify that the right methods are called and that the test get the expected response in the
   * case sending a question and getting an answer
   * 
   * @throws ClientProtocolException
   * @throws IOException
   * @throws SearchException
   * @throws HttpException
   * @throws JSONException
   */
  private void verify_response()
      throws ClientProtocolException, IOException, SearchException, HttpException, JSONException {
    verify(askAQuestionResource).getCsvAnswerData(answers);
    verify(askAQuestionResource).rankAnswers(mocked_csv_answer_data());
    verify(searcher).performSearch(eq(questionText), eq(numOfAnswers));
    assertEquals(expectedResponse, response);
  }

  /***
   * Verify that the right methods are called and that the test get the expected response in the
   * case of a searcher returning an empty set of documents
   * 
   * @throws ClientProtocolException
   * @throws IOException
   * @throws SearchException
   * @throws HttpException
   * @throws JSONException
   */
  private void verify_empty_response()
      throws ClientProtocolException, IOException, SearchException, HttpException, JSONException {
    verify(askAQuestionResource).getCsvAnswerData(answers);
    verify(askAQuestionResource).rankAnswers(mock_empty_csv_answer_data());
    verify(searcher).performSearch(eq(questionText), eq(numOfAnswers));
    assertEquals(expectedResponse, response);
  }

  /**
   * Mock a response of csv data as a response to the call getCsvAnswerData() method
   * 
   * @return a String of non-empty csv data with a header row included
   */
  private String mocked_csv_answer_data() {
    String data = "answer_id,feature0,feature1,feature2,feature3,feature4,feature5,feature6,feature7,"
        + "feature8,feature9,feature10,feature11,feature12,feature13,feature14,feature15,feature16,"
        + "feature17,feature18,feature19,feature20,feature21,feature22,feature23,feature24,feature25,"
        + "feature26,feature27,feature28,feature29,feature30,feature31,ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER,"
        + "ALL_ANSWERS_VOTE_RATIO_FEATURE_SCORER,AUTHOR_REPUTATION_FEATURE_SCORER,NUMBER_OF_ANSWERS_FEATURE_SCORER,"
        + "PAGE_VIEWS_FEATURE_SCORER,VOTE_RATIO_FEATURE_SCORER\n"
        + "1760,2.0120604,0.0,0.0,0.0,0.53256595,0.0,0.0,0.0,0.56830025,0.0,0.0,0.0,4.836687,0.0,0.0,0.0,"
        + "0.6492833,0.0,0.0,0.0,0.9267032,0.0,0.0,0.0,0.6086925,0.0,0.0,0.0,1.0,10.0,0.08701137698962977,4.096962,"
        + "5.0,7.0,559.0,2.0,605.0,1.0";
    return data;
  }

  /**
   * Mock an empty set of csv answer data created by the getCsvAnswerData() call
   * 
   * @return empty csv with header row only
   */
  String mock_empty_csv_answer_data() {
    String data = "answer_id,feature0,feature1,feature2,feature3,feature4,feature5,feature6,feature7,"
        + "feature8,feature9,feature10,feature11,feature12,feature13,feature14,feature15,feature16,"
        + "feature17,feature18,feature19,feature20,feature21,feature22,feature23,feature24,feature25,"
        + "feature26,feature27,feature28,feature29,feature30,feature31,ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER,"
        + "ALL_ANSWERS_VOTE_RATIO_FEATURE_SCORER,AUTHOR_REPUTATION_FEATURE_SCORER,NUMBER_OF_ANSWERS_FEATURE_SCORER,"
        + "PAGE_VIEWS_FEATURE_SCORER,VOTE_RATIO_FEATURE_SCORER\n";
    return data;
  }

  /**
   * Mock the response of ranking a set of answers and returning an array of the ranked answers
   * 
   * @return json of the response
   * @throws IOException
   * @throws JSONException
   */
  private JSONObject mock_ranked_answers() throws IOException, JSONException {
    String data = "{\"ranker_id\":\"9849D4-rank-81\",\"name\":null,\"answers\":"
        + "[{\"score\":20.0,\"confidence\":0.20188169029906117,\"answer_id\":\"" + answerId + "\"}],"
        + "\"top_answer\":\"4248\",\"url\":"
        + "\"https:\\/\\/gateway.watsonplatform.net\\/retrieve-and-rank\\/api\\/v1\\/rankers\\/9849D4-rank-81\"}";
    return new JSONObject(data);
  }

  /**
   * Mock the response of reranking an empty setof answers
   * 
   * @return json object of reranked response
   * @throws IOException
   * @throws JSONException
   */
  private JSONObject mock_empty_ranked_answers() throws IOException, JSONException {
    String data =
        "{\"ranker_id\":\"9849D4-rank-81\",\"name\":null,\"answers\":" + "[]," + "\"top_answer\":\"\",\"url\":"
            + "\"https:\\/\\/gateway.watsonplatform.net\\/retrieve-and-rank\\/api\\/v1\\/rankers\\/9849D4-rank-81\"}";
    return new JSONObject(data);

  }

  /**
   * Run a POST to the server with the question to ask as text
   * 
   * @throws Exception
   */
  private void run_post() throws Exception {
    response = askAQuestionResource.askQuestion(questionText);
  }

  /**
   * Set up a mock of sending a question to the server and getting a non empty response of documents
   * to rank and display
   * 
   * @param questionText the String of the question to ask
   * @throws SearchException
   * @throws ClientProtocolException
   * @throws IOException
   * @throws HttpException
   * @throws JSONException
   */
  private void mock_successful_askqa(String questionText)
      throws SearchException, ClientProtocolException, IOException, HttpException, JSONException {

    // mock rnr searcher response
    mock_successful_searcher();

    // mock sample csv answers
    doReturn(mocked_csv_answer_data()).when(askAQuestionResource).getCsvAnswerData(answers);

    // mock sample ranked answers
    doReturn(mock_ranked_answers()).when(askAQuestionResource).rankAnswers(mocked_csv_answer_data());
  }

  /**
   * Set up a mock of sending a question that throws an exception to the server
   * 
   * @param questionText the String of the question to ask
   * @throws SearchException
   * @throws ClientProtocolException
   * @throws IOException
   * @throws HttpException
   * @throws JSONException
   */
  private void mock_failed_search_askqa(String questionText)
      throws SearchException, ClientProtocolException, IOException, HttpException, JSONException {

    // mock rnr searcher that fails
    mock_failed_searcher();

    // mock sample csv answers
    doReturn(mocked_csv_answer_data()).when(askAQuestionResource).getCsvAnswerData(answers);

    // mock sample ranked answers
    doReturn(mock_ranked_answers()).when(askAQuestionResource).rankAnswers(mocked_csv_answer_data());
  }

  /**
   * Setup a sequence of mock to simulate behaviour when an empty set of documents is returned in
   * response to a query
   * 
   * @param questionText
   * @throws SearchException
   * @throws ClientProtocolException
   * @throws IOException
   * @throws HttpException
   * @throws JSONException
   */
  private void mock_empty_search_askqa(String questionText)
      throws SearchException, ClientProtocolException, IOException, HttpException, JSONException {

    // mock rnr searcher that returns empty set of documents
    mock_empty_searcher();

    // mock sample csv answers
    doReturn(mock_empty_csv_answer_data()).when(askAQuestionResource).getCsvAnswerData(answers);

    // mock sample ranked answers
    doReturn(mock_empty_ranked_answers()).when(askAQuestionResource).rankAnswers(mock_empty_csv_answer_data());
  }

  /**
   * set up a mock of a searcher that returns a document when a search is performed
   * 
   * @throws SearchException
   * @throws IOException
   * @throws JSONException
   */
  private void mock_successful_searcher() throws SearchException, IOException, JSONException {
    answers = new ArrayList<CandidateAnswer>();
    Post p = new Post();
    p.setId(Integer.parseInt(answerId));
    CandidateAnswer answer = new StackExchangeThread(p, null, null, "");
    answer.setAnswerDocumentText(new TextWithAnalysis("hello world"));
    answer.setAnswerTitle(new TextWithAnalysis("title"));
    answers.add(answer);
    expectedResponse = new JSONArray(
        "[{\"site\":\"/questions/42\",\"title\":\"title\",\"text\":\"hello world\",\"questionAuthorUrl\":null,\"answers\":[],\"score\":20.0,\"label\":\"42\","
            + "\"confidence\":0.20188169029906117,\"questionAuthor\":null}]");

    askAQuestionResource.searcher = searcher;
    when(searcher.performSearch(eq(questionText), eq(numOfAnswers))).thenReturn(answers);
  }

  /**
   * Set up a mock of a searcher that throws an exception when a search is performed
   * 
   * @throws SearchException
   */
  private void mock_failed_searcher() throws SearchException {
    askAQuestionResource.searcher = searcher;
    when(searcher.performSearch(eq(questionText), eq(numOfAnswers))).thenThrow(new SearchException("test exception"));
  }

  /**
   * Set up a mock of a searcher that returns an empty set of documents as results to a query
   * 
   * @throws SearchException
   * @throws IOException
   * @throws JSONException
   */
  private void mock_empty_searcher() throws SearchException, IOException, JSONException {
    askAQuestionResource.searcher = searcher;

    answers = new ArrayList<CandidateAnswer>();
    expectedResponse = new JSONArray("[]");

    when(searcher.performSearch(eq(questionText), eq(numOfAnswers))).thenReturn(new ArrayList<CandidateAnswer>());
  }
}
