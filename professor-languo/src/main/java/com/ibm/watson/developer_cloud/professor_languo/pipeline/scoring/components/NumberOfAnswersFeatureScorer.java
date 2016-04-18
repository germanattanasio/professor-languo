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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components;

import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

public class NumberOfAnswersFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Generate the feature score pair for the "Author Reputation" This will calculate the number of
   * answers the candidate answer thread has.
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   * @return a FeatureScorePair object of the feature's key and the calculated value; the value is
   *         the number of answers the candidate answer thread has.
   */
  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {
    StackExchangeThread candidateAnswerThread = (StackExchangeThread) candidateAnswer;
    double score = transformScore(candidateAnswerThread.getAnswers().size());
    candidateAnswer.setFeatureValue(FeatureScorerEnums.NUMBER_OF_ANSWERS_FEATURE_SCORER.toString(), score);
  }

  /**
   * @param score - a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }

}
