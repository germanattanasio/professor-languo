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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;
import com.ibm.watson.developer_cloud.professor_languo.endpoints.GetThreadResource;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;

public class GetThreadResourceTests {
  GetThreadResource getThreadResource = new GetThreadResource();
  RetrieveAndRankSearcher searcher;
  Response response;
  String id = "10";
  String differentId = "42";
  String badId = "bad";
  GetThreadResourceTests WHEN = this, THEN = this;
  private String expectedResponse;

  /**
   * Reset variables before each test
   */
  @org.junit.Before public void initialize() {
    searcher = null;
    response = null;
    getThreadResource.searcher = null;
  }

  /**
   * Test what happens when we send an id and get a response of matching documents
   * 
   * @throws Exception
   */
  @Test public void run_test_with_docid_in_index() throws Exception {
    WHEN.mock_single_doc_response(id);
    THEN.run_get();
    THEN.response_must_be_doc();
  }

  /**
   * Test what happens when we send an id but no matching documents are in the index
   * 
   * @throws Exception
   */
  @Test public void run_test_without_docid_in_index() throws Exception {
    WHEN.mock_single_doc_response(id);
    THEN.run_get_with_different_id();
    THEN.response_must_not_be_doc();
  }

  /**
   * Test what happens when the id sent is not a properly formated number
   * 
   * @throws Exception
   */
  @Test(expected = NumberFormatException.class) public void run_test_with_bad_id() throws Exception {
    WHEN.mock_single_doc_response(id);
    THEN.run_get_with_bad_id();
    THEN.response_must_be_bad();
  }

  /**
   * Test what happens when ingestion is performed poorly and more that one document matches the id
   * 
   * @throws Exception
   */
  @Test public void run_test_with_bad_ingestion() throws Exception {
    WHEN.mock_multi_doc_response(id);
    THEN.run_get();
    THEN.response_must_be_bad();
  }

  /**
   * Asser that a 200 response called is returned and that data on the doc is returned
   */
  private void response_must_be_doc() {
    assertEquals(200, response.getStatus());
    assertEquals(expectedResponse, response.getEntity().toString());
  }

  /**
   * Assert that a 500 error is thrown
   */
  private void response_must_be_bad() {
    assertEquals(500, response.getStatus());
    if (expectedResponse != null) {
      assertEquals(expectedResponse, response.getEntity().toString());
    }
  }

  /**
   * Assert that the response is a 404 error with an error stating the doc wasn't found in the index
   */
  private void response_must_not_be_doc() {
    assertEquals(404, response.getStatus());
    expectedResponse = MessageFormat.format(Messages.getString("RetrieveAndRank.THREAD_ID_NOT_FOUND"), differentId);
    assertEquals(expectedResponse, response.getEntity().toString());
  }

  /**
   * Send a get with an id
   * 
   * @throws Exception
   */
  private void run_get() throws Exception {
    response = getThreadResource.getThread(id);
  }

  /**
   * Send a GET with an id not in the index
   * 
   * @throws Exception
   */
  private void run_get_with_different_id() throws Exception {
    response = getThreadResource.getThread(differentId);
  }

  /**
   * Send a GET with a poorly formated id
   * 
   * @throws Exception
   */
  private void run_get_with_bad_id() throws Exception {
    response = getThreadResource.getThread(badId);
  }

  /**
   * Setup the mocked searcher variable
   */
  private void mock_searcher() {
    searcher = mock(RetrieveAndRankSearcher.class);
    getThreadResource.searcher = searcher;
  }

  /**
   * Mock the searcher responding with one doc matching the id
   * 
   * @param id
   * @throws SearchException
   */
  private void mock_single_doc_response(String id) throws SearchException {
    String query = IndexDocumentFieldName.THREAD_POST_ID.toString() + ":" + id;
    mock_searcher();

    Collection<CandidateAnswer> answers = new ArrayList<CandidateAnswer>();
    CandidateAnswer answer = new CandidateAnswer(id, id);
    answer.setAnswerDocumentText(new TextWithAnalysis("hello world"));
    answers.add(answer);
    expectedResponse = "<!DOCTYPE html>" + answer.getAnswerDocumentText().getText();

    when(searcher.performSearch(eq(query))).thenReturn(answers);
  }

  /**
   * Mock the searcher responding with multiple docs matching the given id
   * 
   * @param id
   * @throws SearchException
   */
  private void mock_multi_doc_response(String id) throws SearchException {
    String query = IndexDocumentFieldName.THREAD_POST_ID.toString() + ":" + id;
    mock_searcher();

    Collection<CandidateAnswer> answers = new ArrayList<CandidateAnswer>();
    CandidateAnswer answer1 = new CandidateAnswer(id, id);
    answer1.setAnswerDocumentText(new TextWithAnalysis("hello world"));
    answers.add(answer1);

    CandidateAnswer answer2 = new CandidateAnswer(id, id);
    answer2.setAnswerDocumentText(new TextWithAnalysis("hello world again"));
    answers.add(answer2);

    expectedResponse = MessageFormat.format(Messages.getString("RetrieveAndRank.THREAD_ID_NOT_UNIQUE"), id); //$NON-NLS-1$

    when(searcher.performSearch(eq(query))).thenReturn(answers);
  }

}
