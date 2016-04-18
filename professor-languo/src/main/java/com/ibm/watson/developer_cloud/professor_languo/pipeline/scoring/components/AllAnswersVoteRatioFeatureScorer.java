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
import java.util.Set;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

public class AllAnswersVoteRatioFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Generate the feature score pair for the "All Answers Vote Ratio" This will calculate the ratio
   * between the up and down votes of all the answers provided to the candidateAnswer's thread.
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   */
  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {
    StackExchangeThread candidateAnswerThread = (StackExchangeThread) candidateAnswer;
    Set<StackExchangeAnswer> answersExchangeThread = candidateAnswerThread.getAnswers();

    long answerUpVotesCount = 0;
    long answerDownVotesCount = 0;

    for (StackExchangeAnswer answer : answersExchangeThread) {
      answerUpVotesCount += answer.getVoteCount(VoteType.UP_MOD);
      answerDownVotesCount += answer.getVoteCount(VoteType.DOWN_MOD);
    }

    double score = VotesRatioUtil.calculateVoteRatio(answerUpVotesCount, answerDownVotesCount,
        AnswerScorerConstants.VOTE_RATIO_FACTOR_ALL_ANSWERS_VOTES);
    candidateAnswer.setFeatureValue(FeatureScorerEnums.ALL_ANSWERS_VOTE_RATIO_FEATURE_SCORER.toString(),
        transformScore(score));
  }

  /**
   * @param score a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }

}
