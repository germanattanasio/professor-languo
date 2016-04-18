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

import java.util.List;
import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

/**
 * Scorer to incorporate tags into the ranking process, since hard filtering on tags would be too
 * harsh. Reward the answer threads that have more tags in common with the question. This scorer
 * needs a {@link Question} to calculate its score, so a custom
 * {@code generateFeatureScore(CandidateAnswer, Question)} method, is called instead of the
 * interface method {@code generateFeatureScore(CandidateAnswer)}
 *
 */
public class TagSimilarityFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Score an answer based on number of tags that match
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   * @param question - the user's question
   */
  public void generateFeatureScore(CandidateAnswer candidateAnswer, Question question) throws AnswerScorerException {
    StackExchangeQuestion seQuestion = (StackExchangeQuestion) question;

    // if the question has no tags, set the score to 0
    List<String> questionTags = seQuestion.getTags();
    if (questionTags == null || questionTags.size() == 0) {
      candidateAnswer.setFeatureValue(FeatureScorerEnums.TAG_SIMILARITY_FEATURE_SCORER.toString(), 0.0);
      return;
    }

    // compare tags of the answer thread and the tags of the question
    StackExchangeThread candidateAnswerThread = (StackExchangeThread) candidateAnswer;
    List<String> answerTags = candidateAnswerThread.getQuestion().getTags();
    double score = 0;
    for (String qTag : questionTags) {
      for (String aTag : answerTags) {
        if (qTag.equalsIgnoreCase(aTag)) {
          // if there is a mutual tag, increment
          score++;
          break;
        }
      }
    }

    // set the count as the score
    candidateAnswer.setFeatureValue(FeatureScorerEnums.TAG_SIMILARITY_FEATURE_SCORER.toString(), score);
  }

  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {}

  /**
   * @param score - a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }
}
