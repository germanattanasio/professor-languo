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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Vote;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

public class AcceptedAnswerVoteRatioFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Generate the feature score pair for the "Accepted Answer's Vote Ratio" This will calculate the
   * ratio between the up and down votes of the accepted answer to the provided candidateAnswer.
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   */
  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {
    StackExchangeThread candidateAnswerThread = (StackExchangeThread) candidateAnswer;
    StackExchangeAnswer acceptedAnswer = candidateAnswerThread.getAcceptedAnswer();

    if (acceptedAnswer == null) {
      candidateAnswer.setFeatureValue(FeatureScorerEnums.ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER.toString(), 0.0);
      return;
    }

    Map<VoteType, Set<Vote>> votes = acceptedAnswer.getVoteMap();
    long upVotes = votes.get(VoteType.UP_MOD) != null ? votes.get(VoteType.UP_MOD).size() : 0;
    long downVotes = votes.get(VoteType.DOWN_MOD) != null ? votes.get(VoteType.DOWN_MOD).size() : 0;

    double score = transformScore(VotesRatioUtil.calculateVoteRatio(upVotes, downVotes,
        AnswerScorerConstants.VOTE_RATIO_FACTOR_ACCEPTED_ANSWER_VOTES));

    candidateAnswer.setFeatureValue(FeatureScorerEnums.ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER.toString(), score);
  }

  /**
   * @param score - a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }
}
