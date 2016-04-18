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
package com.ibm.watson.developer_cloud.professor_languo.pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;

import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerMergerAndRanker;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.RankerCreationUtil;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcherConstants;

/**
 * This class creates a ranker from the Retrieve and Rank service. It creates the training data csv
 * file located at {@code trainingFilePath} whcih is formatted as below:
 * 
 * question_id,{list of feature values separated by commas},ground_truth
 * 
 * The header of this file must be consistent during training and testing. This class is also
 * designed to work with the qa-framework
 *
 */

public class RnrMergerAndRanker implements AnswerMergerAndRanker {
  private final static Logger logger = LogManager.getLogger(RankerCreationUtil.class.getCanonicalName());

  private static final AtomicInteger QIDGenerator = new AtomicInteger(0);

  public String trainingFilePath;
  public String base_url;
  private String cluster, collection, rankerName;
  private Credentials creds;
  private int rows;
  private static String ranker_url;
  private String current_ranker_id;
  private boolean addHeader;

  private CloseableHttpClient client;
  private int count;
  private int goodRecallCount;
  private int retryLimit;
  private StringBuffer trainingData;

  /**
   * Initialize the AnswerMergerAndRanker with the {@link Properties} object obtained from the
   * properties file
   */
  @Override public void initialize(Properties properties) {
    creds = new UsernamePasswordCredentials(properties.getProperty(RetrieveAndRankConstants.USERNAME),
        properties.getProperty(RetrieveAndRankConstants.PASSWORD));
    cluster = properties.getProperty(RetrieveAndRankConstants.SOLR_CLUSTER_ID);
    collection = properties.getProperty(RetrieveAndRankConstants.COLLECTION);
    rankerName = properties.getProperty(RetrieveAndRankConstants.RANKER_NAME);
    rows = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.CANDIDATE_ANSWER_NUM));
    base_url = properties.getProperty(RetrieveAndRankConstants.RNR_ENDPOINT);
    trainingFilePath = properties.getProperty(RetrieveAndRankConstants.TRAINING_DATA_PATH);
    retryLimit = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.QUERY_RETRY_LIMIT));
    addHeader = true;
    trainingData = new StringBuffer();

    ranker_url = base_url + RetrieveAndRankConstants.RNR_ENDPOINT_VERSION
        + RetrieveAndRankSearcherConstants.RANKER_REQUEST_HANDLER;

    if (cluster == null || collection == null) {
      logger.info(Messages.getString("RetrieveAndRank.MISSING_PROPERTY")); //$NON-NLS-1$
      return;
    }

    logger.info(Messages.getString("RetrieveAndRank.SOLAR_CLUSTER_ID") + cluster); //$NON-NLS-1$
    logger.info(Messages.getString("RetrieveAndRank.SOLAR_COLLECTION_ID") + collection); //$NON-NLS-1$
    logger.info(Messages.getString("RetrieveAndRank.RANKER_NAME") + rankerName); //$NON-NLS-1$
    logger.info(Messages.getString("RetrieveAndRank.SOLAR_ROWS") + rows); //$NON-NLS-1$

    // Keep track of recall ratio
    count = 0;
    goodRecallCount = 0;
  }

  /**
   * If {@code correctAnswers != null}, this method will create the training data csv file from the
   * list of CandidateAnswers and return {@code answers} unchanged. Else, it will apply the ranker
   * created in the current invocation of this class.
   * 
   * @param question {@link Question} from which {@code answers} are generated
   * @param answers List of {@link CandidateAnswer}s
   * @param correctAnswers List of {@link CorrectAnswer}s to the {@code question}. There should only
   *        be one correct answer for each question.
   * 
   */
  @Override public Observable<CandidateAnswer> mergeAndRankAnswers(Question question,
      Observable<CandidateAnswer> answers, Collection<CorrectAnswer> correctAnswers) {

    client = RankerCreationUtil.createHttpClient(AuthScope.ANY, creds);

    if (correctAnswers != null) { // TRAINING PHASE
      train(question, answers, correctAnswers);
      return answers;
    } else { // TESTING PHASE
      return apply(question, answers);
    }
  }

  @Override
  /**
   * Runs at the end of the training phase, invoked by a QuestionAnswerer
   */
  public void finishTraining() {
    client = RankerCreationUtil.createHttpClient(AuthScope.ANY, creds);
    long startTime = System.currentTimeMillis();

    try {
      // Attempt to train the ranker with the training file
      logger.info(Messages.getString("RetrieveAndRank.RANKER_ATTEPT_CREATE")); //$NON-NLS-1$
      String result = RankerCreationUtil.trainRanker(ranker_url, rankerName, client, trainingData);

      logger.info(Messages.getString("RetrieveAndRank.RANKER_WRITE_TO_DISK_START"));
      BufferedWriter bw = new BufferedWriter(new FileWriter(trainingFilePath));
      bw.write(trainingData.toString());
      bw.close();
      logger.info(Messages.getString("RetrieveAndRank.RANKER_WRITE_TO_DISK_END"));

      // Obtain ranker_id
      JSONObject jsonResult = (JSONObject) JSON.parse(result);
      String ranker_id = (String) jsonResult.get("ranker_id");
      current_ranker_id = ranker_id;

      // Ping the ranker for its status
      String status = getRankerStatus(ranker_id);
      logger.info(Messages.getString("RetrieveAndRank.RANKER_TRAINING_KICKOFF") + trainingFilePath); //$NON-NLS-1$

      // waiting for training to complete
      while (!status.equals("Available")) {
        logger.info(Messages.getString("RetrieveAndRank.RANKER_STATUS") + status); //$NON-NLS-1$
        try {
          Thread.sleep(30000);
        } catch (InterruptedException e) {
          throw new RuntimeException(Messages.getString("RetrieveAndRank.RANKER_TRAINING_INTERRUPTED"), e);
        }
        status = getRankerStatus(ranker_id);
        if (status.equals("Failed"))
          throw new RuntimeException(Messages.getString("RetrieveAndRank.RANKER_TRAINING_FAIL"));
      }
      long timeTaken = System.currentTimeMillis() - startTime;
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.RANKER_TRAINING_FINISH"), //$NON-NLS-1$
          ranker_id, DurationFormatUtils.formatDurationWords(timeTaken, true, false)));
    } catch (IOException | NullPointerException | JSONException e) {
      logger.error(e.getMessage());
    }
    logger.info("Ranker ID: " + current_ranker_id);
  }

  /**
   * Save feature vectors of each candidate answer to a CSV file to send to the ranker for training
   * 
   * @param question
   * @param answers
   * @param correctAnswers
   */
  private void train(Question question, Observable<CandidateAnswer> answers, Collection<CorrectAnswer> correctAnswers) {

    List<CandidateAnswer> candidate_answers = answers.toList().toBlocking().first();
    // There may be no candidate answers if the query is completely
    // unrelated
    // to the corpus
    if (candidate_answers.size() == 0) {
      logger.info(Messages.getString("RetrieveAndRank.RANKER_NO_CANDIDATE_ANSWER")); //$NON-NLS-1$
      return;
    }

    // Check if the corret answer exists in the list of candidate answers
    boolean correctAnswerExists = false;
    for (CandidateAnswer ca : candidate_answers) {
      if (CorrectAnswer.isCorrect(ca, correctAnswers)) {
        correctAnswerExists = true;
        break;
      }
    }
    if (correctAnswerExists) {
      // Get list of features
      CandidateAnswer sampleAnswer = candidate_answers.get(0);
      Object[] objArray = sampleAnswer.getFeatures().toArray();
      String[] features = Arrays.copyOf(objArray, objArray.length, String[].class);

      if (addHeader) {
        Set<Entry<String, Double>> headerAnswerPairs = sampleAnswer.getFeatureValuePairs();
        List<String> headers = new ArrayList<String>();
        headers.add(RetrieveAndRankConstants.QUESTION_ID_HEADER);
        Iterator<Entry<String, Double>> it = headerAnswerPairs.iterator();
        while (it.hasNext()) {
          Entry<String, Double> entry = (Entry<String, Double>) it.next();
          headers.add(entry.getKey());
        }
        writeHeader(headers, true);
      }

      try {
        writeAnswers(candidate_answers, correctAnswers, question, features);
      } catch (IOException e) {
        throw new RuntimeException(Messages.getString("RetrieveAndRank.RANKER_WRITE_ERROR") + e); //$NON-NLS-1$
      }

      addHeader = false;
      goodRecallCount++;
      if (goodRecallCount % 10 == 0) {
        logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.RANKER_RECALL_NUMBER"), //$NON-NLS-1$
            goodRecallCount, count));
      }
    }
    count++;
  }

  /**
   * Rank {@code answers} to {@code question}
   * 
   * @param question {@link Question} from which {@link answers} are formed
   * @param answers list of {@link CandidateAnswer}'s
   * @return Ranked answers
   */
  private Observable<CandidateAnswer> apply(Question question, Observable<CandidateAnswer> answers) {

    try {
      // Create authorized HttpClient
      CloseableHttpClient client = RankerCreationUtil.createHttpClient(AuthScope.ANY, creds);

      // Build feature vector data for candidate answers in csv format
      String csvAnswerData = RankerCreationUtil.getCsvAnswerData(answers.toList().toBlocking().first(), null);

      // Send rank request
      String rank_request_url =
          ranker_url + "/" + current_ranker_id + RetrieveAndRankSearcherConstants.RANK_REQUEST_HANDLER;
      JSONObject responseJSON = RankerCreationUtil.rankAnswers(client, rank_request_url, csvAnswerData);
      JSONArray rankedAnswerArray = (JSONArray) responseJSON.get("answers");

      // If there is an error with the service, wait a moment
      // and retry up to retry limit
      int retryAttempt = 1;
      while (rankedAnswerArray == null) {
        if (retryAttempt > retryLimit) {
          throw new PipelineException(
              MessageFormat.format(Messages.getString("RetrieveAndRank.QUERY_RETRY_FAILED"), retryAttempt)); //$NON-NLS-1$
        }
        Thread.sleep(3000);
        logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.QUERY_RETRY"), retryAttempt)); //$NON-NLS-1$
        responseJSON = RankerCreationUtil.rankAnswers(client, rank_request_url, csvAnswerData);
        rankedAnswerArray = (JSONArray) responseJSON.get("answers");
        retryAttempt++;
      }

      // Iterate through JSONArray of ranked answers and match with the
      // original
      List<CandidateAnswer> answerList = answers.toList().toBlocking().first();

      // Set confidence to the top answers chosen by the ranker,
      // ignore the rest
      List<CandidateAnswer> rankedAnswerList = new ArrayList<CandidateAnswer>();

      for (int i = 0; i < answerList.size(); i++) {
        for (int j = 0; j < rankedAnswerArray.size(); j++) {
          JSONObject ans = (JSONObject) rankedAnswerArray.get(j);
          // Get the answer_id
          String answer_id = (String) ans.get(RetrieveAndRankConstants.ANSWER_ID_HEADER);
          double confidence = (double) ans.get(RetrieveAndRankConstants.CONFIDENCE_HEADER);
          if (answerList.get(i).getAnswerLabel().equals(answer_id)) {
            // Set the answer's confidence
            answerList.get(i).setConfidence(confidence);
            rankedAnswerList.add(answerList.get(i));
          }
        }
      }

      return Observable.from(rankedAnswerList);

    } catch (ClientProtocolException e) {
      logger.error(e.getMessage());
    } catch (IOException e) {
      logger.error(e.getMessage());
      // Something wrong with the service. Set all confidence to 0
      List<CandidateAnswer> answerList = answers.toList().toBlocking().first();
      for (CandidateAnswer answer : answerList) {
        answer.setConfidence(0);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return answers;
  }

  /**
   * Gets the rankers associated with the given credentials
   * 
   * @return a list of ranker_ids of the active rankers
   * @throws ClientProtocolException
   * @throws IOException
   */
  public List<String> getRankers() throws ClientProtocolException, IOException {

    List<String> rankerIds = new ArrayList<String>();
    JSONArray rankers;
    // Create authorized HttpClient
    client = RankerCreationUtil.createHttpClient(AuthScope.ANY, creds);

    try {
      HttpGet httpget = new HttpGet(ranker_url);
      CloseableHttpResponse response = client.execute(httpget);
      try {
        String result = EntityUtils.toString(response.getEntity());
        JSONObject jobject = (JSONObject) JSON.parse(result);
        rankers = jobject.getJSONArray("rankers");

        for (int i = 0; i < rankers.size(); i++) {
          rankerIds.add((String) ((JSONObject) rankers.get(i)).get("ranker_id"));
        }
      } catch (NullPointerException | JSONException e) {
        logger.error(e.getMessage());
      }

      finally {
        response.close();
      }
    }

    finally {
      client.close();
    }

    return rankerIds;
  }

  /**
   * Returns the status of the specified ranker
   * 
   * @param ranker_id of the ranker
   * @return status. e.g. "Available", "Training", "Failed"
   * @throws IOException
   */
  public String getRankerStatus(String ranker_id) throws IOException {

    String status = null;
    JSONObject res;
    // Create authorized HttpClient
    client = RankerCreationUtil.createHttpClient(AuthScope.ANY, creds);

    try {
      HttpGet httpget = new HttpGet(ranker_url + "/" + ranker_id);
      CloseableHttpResponse response = client.execute(httpget);

      try {

        String result = EntityUtils.toString(response.getEntity());
        res = (JSONObject) JSON.parse(result);
        status = (String) res.get("status");

      } catch (NullPointerException | JSONException e) {
        logger.error(e.getMessage());
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

  /**
   * Deletes the specified ranker
   * 
   * @param ranker_id ofthe ranker to be deleted
   * @throws ClientProtocolException
   * @throws IOException
   * @throws JSONException
   */
  public static void deleteRanker(CloseableHttpClient client, String ranker_id)
      throws ClientProtocolException, IOException {

    JSONObject res;

    try {
      HttpDelete httpdelete = new HttpDelete(ranker_url + "/" + ranker_id);
      httpdelete.setHeader("Content-Type", "application/json");
      CloseableHttpResponse response = client.execute(httpdelete);

      try {

        String result = EntityUtils.toString(response.getEntity());
        res = (JSONObject) JSON.parse(result);
        if (res.isEmpty()) {
          logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.RANKER_DELETE"), //$NON-NLS-1$
              ranker_id));
        } else {
          logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.RANKER_DELETE_FAIL"), //$NON-NLS-1$
              ranker_id));
        }
      } catch (NullPointerException | JSONException e) {
        logger.error(e.getMessage());
      }

      finally {
        response.close();
      }
    }

    finally {
      client.close();
    }
  }

  /**
   * This method writes the header required by RaaS training scripts, Retrieve & Rank service, etc.
   * to the training data file.
   * 
   * @param trainFile
   * @param numOfFeatures
   */
  private void writeHeader(List<String> headers, boolean addGroundTruth) {
    for (int i = 0; i < headers.size(); i++) {
      trainingData.append(headers.get(i) + ",");
    }
    if (addGroundTruth) {
      trainingData.append("ground_truth");
      trainingData.append("\n");
    } else {
      trainingData.delete(trainingData.length() - 1, trainingData.length());
      trainingData.append("\n");
    }
  }

  /**
   * This method writes the header required by RaaS training scripts, Retrieve & Rank service, etc.
   * to the training data file.
   * 
   * @param trainFile
   * @param numOfFeatures
   */
  private void writeAnswers(List<CandidateAnswer> answers, Collection<CorrectAnswer> correctAnswers, Question question,
      String[] features) throws IOException {

    int qid = getQID();
    for (CandidateAnswer answer : answers) {

      trainingData.append(qid);

      for (int i = 0; i < features.length; i++) {
        Double featureValue = answer.getFeatureValue(features[i]);
        if (featureValue == null)
          featureValue = 0d;
        trainingData.append("," + featureValue);
      }
      trainingData.append("," + (CorrectAnswer.isCorrect(answer, correctAnswers) == true ? 1 : 0));
      trainingData.append("\n");
    }
  }

  /**
   * Helper method that assigns QIDs to each thread (per question) in serial order (as required by
   * RaaS)
   * 
   * @return the QID to be used for feature-vectors of the candidate answers of the current question
   */

  public synchronized int getQID() {
    return QIDGenerator.incrementAndGet();
  }

}
