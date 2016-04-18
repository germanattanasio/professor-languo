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
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

public class VoteRatioFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Generate the feature score pair for the "Vote Ratio" This will calculate the ratio between the
   * up and down votes of the candidate answer
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   */
  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {
    StackExchangeThread answerThread = (StackExchangeThread) candidateAnswer;

    long upVotesCount = answerThread.getVoteCount(VoteType.UP_MOD);
    long downVotesCount = answerThread.getVoteCount(VoteType.DOWN_MOD);

    double score = transformScore(VotesRatioUtil.calculateVoteRatio(upVotesCount, downVotesCount,
        AnswerScorerConstants.VOTE_RATIO_FACTOR_CANDIDATE_ANSWER));
    candidateAnswer.setFeatureValue(FeatureScorerEnums.VOTE_RATIO_FEATURE_SCORER.toString(), score);
  }

  /**
   * @param score a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }

}
