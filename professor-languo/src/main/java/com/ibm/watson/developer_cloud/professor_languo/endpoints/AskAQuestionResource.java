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

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.RankerCreationUtil;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.BaseEgaMetaDataAnswerScorer;

/**
 * An endpoint to ask send questions to the server and get a ranked response of documents answers
 * from the index
 *
 */
@Path("/ask_question")
public class AskAQuestionResource {
  private final static Logger logger = LogManager.getLogger(AskAQuestionResource.class);
  private final static Logger questionLogger = LogManager.getLogger("questionLogger");
  private final static String QUESTION_URL = "/questions/";
  private final static String USERS_URL = "/users/";
  RetrieveAndRankSearcher searcher = new RetrieveAndRankSearcher();
  BaseEgaMetaDataAnswerScorer scorer = new BaseEgaMetaDataAnswerScorer();
  private String username;
  private String password;
  private String ranker_id;
  private String ranker_url;
  private CloseableHttpClient client;

  /**
   * Receives a populated form with the user's question and returns a JSON Array of answer titles.
   * 
   * @param questionText The question as a string
   * @return A JSONArray of answers with titles, text, labels and confidences
   * @throws Exception
   */
  @POST @Consumes("application/x-www-form-urlencoded") @Produces(MediaType.APPLICATION_JSON) public JSONArray askQuestion(
      @FormParam("questionText") String questionText) throws Exception {

    // Get the question sanitized
    questionText = questionText.trim().replaceAll(":", "\\\\:").replaceAll("\"", "\\\\\"").replaceAll("\\{", "");
    questionLogger.info(questionText);

    // Configure, based on a properties file, how the pipeline will
    // answer questions.
    Properties appProperties = new Properties();
    appProperties.load(this.getClass().getResourceAsStream("/app_config.properties"));

    // loading configs from the server.env file
    try {
      IndexerAndSearcherFactory.loadStaticBluemixProperties(appProperties);
    } catch (IOException | IllegalArgumentException e) {
      logger.error(e);
    }

    /**
     * The Server.env file follows this format:
     * 
     * VCAP_SERVICES={"retrieve_and_rank": [{"name": "Watson Retrieve and Rank-yl","label":
     * "retrieve_and_rank","plan": "experimental","credentials": {"url":
     * "https://gateway.watsonplatform.net/retrieve-and-rank/api", "username":
     * "8a6a5c6f-9e17-4200-ac53-ffc00cef0542", "password": "656pgU0JNg7p" } } ] }
     * SOLR_CLUSTER_ID=sc13ea22b7_ab63_4da6_b321_8ddf9a1a8999 CLUSTER_SIZE=7 CLUSTER_NAME=null
     * RANKER_ID=null RANKER_NAME=null TRAINING_DATA_PATH=trainingdata_unix.csv
     * COLLECTION=unix_collection CONFIG_NAME=rnr_config
     * CONFIG_PATH=src/main/resources/solrConfig/solrConfig.zip
     * 
     */

    // get the credentials and urls
    initialize(appProperties);

    // Create a searcher and get answers to the question

    searcher.initialize(appProperties);

    Collection<CandidateAnswer> answers = searcher.performSearch(questionText,
        Integer.parseInt(appProperties.getProperty(RetrieveAndRankConstants.CANDIDATE_ANSWER_NUM)));

    scorer.initialize(appProperties);

    // Join the header and body to build the entire csv answer data to send
    String csvAnswerData = getCsvAnswerData(answers);
    logger.debug(csvAnswerData);

    // Read re-ranked answers
    JSONObject responseJSON = rankAnswers(csvAnswerData);
    logger.debug(responseJSON);

    JSONArray answerArray = (JSONArray) responseJSON.get("answers");
    logger.debug(answerArray);

    if (answerArray == null) {
      return new JSONArray();
    } else {
      JSONArray results = buildPopulatedJSONArray(answerArray, answers);
      return results;
    }
  }

  /**
   * Ranker the answers provided as a CSV
   * 
   * @param csvAnswerData the input data of CSV answers to rank
   * @return a json object of the ranked answers
   * @throws ClientProtocolException
   * @throws IOException
   * @throws HttpException
   * @throws JSONException
   */
  JSONObject rankAnswers(String csvAnswerData)
      throws ClientProtocolException, IOException, HttpException, JSONException {
    return RankerCreationUtil.rankAnswers(client, ranker_url, csvAnswerData);
  }

  /**
   * Returns a CSV of the answers with their scored features included
   * 
   * @param answers
   * @return a String of CSV data
   */
  String getCsvAnswerData(Collection<CandidateAnswer> answers) {
    return RankerCreationUtil.getCsvAnswerData(answers, scorer);

  }

  /**
   * Builds a new safe JSONArray with JSONObjects containing the answer data from a JSONArray
   * containing answerIds
   * 
   * @param questionInfoArray the JSONArray with answerIds
   * @param questions a collection of the CandidateAnswers that match the ids in the answerArray
   * @return
   * @throws JSONException
   */
  private JSONArray buildPopulatedJSONArray(JSONArray questionInfoArray, Collection<CandidateAnswer> questions)
      throws JSONException {

    // Build JSONarray with the question thread data to be returned
    JSONArray results = new JSONArray();
    for (int i = 0; i < questionInfoArray.size(); i++) {
      JSONObject questionInfo = (JSONObject) questionInfoArray.get(i);
      String question_id = (String) questionInfo.get(RetrieveAndRankConstants.ANSWER_ID_HEADER);

      // find the reranked answer and create a JSONObject for display in
      // the UI
      for (CandidateAnswer question : questions) {
        if (question.getAnswerLabel() != null && question.getAnswerLabel().equals(question_id)) {
          JSONObject jsonQuestionThread = new JSONObject();
          jsonQuestionThread.put("text",
              question.getAnswerDocumentText() != null ? question.getAnswerDocumentText().getText() : null);
          jsonQuestionThread.put("title",
              question.getAnswerTitle() != null ? question.getAnswerTitle().getText() : null);
          jsonQuestionThread.put("label", question.getAnswerLabel());
          JSONArray questionAnswerArray = new JSONArray();
          Set<StackExchangeAnswer> questionAnswers = ((StackExchangeThread) question).getAnswers();

          String site = ((StackExchangeThread) question).getSite();

          jsonQuestionThread.put("confidence", questionInfo.get("confidence"));
          jsonQuestionThread.put("score", questionInfo.get("score"));
          jsonQuestionThread.put("questionAuthor", ((StackExchangeThread) question).getAuthor() != null
              ? ((StackExchangeThread) question).getAuthor().getDisplayName() : null);
          jsonQuestionThread.put("questionAuthorUrl", ((StackExchangeThread) question).getAuthor() != null
              ? site + USERS_URL + ((StackExchangeThread) question).getAuthor().getId() : null);
          jsonQuestionThread.put("site", site + QUESTION_URL + ((StackExchangeThread) question).getId());

          // create a JSON object with an answer field that will be
          // returned in the array of answers for this particular post
          for (StackExchangeAnswer questionAnswer : questionAnswers) {
            JSONObject answerObject = new JSONObject();

            answerObject.put("answerAuthor",
                questionAnswer.getAuthor() != null ? questionAnswer.getAuthor().getDisplayName() : null);
            answerObject.put("answerAuthorUrl",
                questionAnswer.getAuthor() != null ? site + USERS_URL + questionAnswer.getAuthor().getId() : null);
            answerObject.put("site",
                questionAnswer.getAnswer() != null ? site + QUESTION_URL + questionAnswer.getAnswer().getId() : null);
            answerObject.put("answer",
                questionAnswer.getAnswer() != null ? questionAnswer.getAnswer().getBody() : null);

            // If this is the accepted answer add it to the accepted
            // answer field of this question
            if (((StackExchangeThread) question).getAcceptedAnswer() != null
                && questionAnswer.getId() == ((StackExchangeThread) question).getAcceptedAnswer().getId()) {
              jsonQuestionThread.put("acceptedAnswer", answerObject);
            }

            questionAnswerArray.add(answerObject);
          }

          jsonQuestionThread.put("answers", questionAnswerArray);
          results.add(jsonQuestionThread);
          break;
        }
      }
    }
    return results;
  }

  /**
   * Read the credentials and urls from the properties
   * 
   * @param appProperties properties object to read from
   */
  private void initialize(Properties appProperties) {

    logger.debug(appProperties.entrySet().toString());

    // read credentials and endpoint urls
    username = appProperties.getProperty(RetrieveAndRankConstants.USERNAME, "");
    password = appProperties.getProperty(RetrieveAndRankConstants.PASSWORD, "");
    final String retrieve_and_rank_endpoint = appProperties.getProperty(RetrieveAndRankConstants.RNR_ENDPOINT, "");
    ranker_id = appProperties.getProperty(RetrieveAndRankConstants.RANKER_ID, "");
    ranker_url = retrieve_and_rank_endpoint + RetrieveAndRankConstants.RNR_ENDPOINT_VERSION
        + RetrieveAndRankConstants.RANKERS_URL + ranker_id + RetrieveAndRankConstants.RERANK_URL;
    checkStrings(username, password, retrieve_and_rank_endpoint, ranker_id, ranker_url);

    // connect the client
    client = RankerCreationUtil.createHttpClient(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
  }

  /**
   * Checks that strings are not blank or null
   * 
   * @param strings
   * @throws IllegalArgumentException if any of the string arguments is blank or null
   */
  private void checkStrings(String... strings) throws IllegalArgumentException {
    for (String string : strings) {
      if (StringUtils.isBlank(string)) {
        String message = Messages.getString("RetrieveAndRank.BAD_CREDENTIAL");
        logger.error(message);
        throw new IllegalArgumentException(message);
      }
    }
  }

}
