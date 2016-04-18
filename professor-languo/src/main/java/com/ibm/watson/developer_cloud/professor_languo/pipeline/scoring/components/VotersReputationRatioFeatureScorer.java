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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.User;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Vote;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;

public class VotersReputationRatioFeatureScorer implements EgaMetaDataScorerComponent {

  @Override public void initialize(Properties properties) throws AnswerScorerException {

  }

  /**
   * Generate the feature score pair for the "Vote Ratio" This will calculate the ratio between the
   * total reputation of all voters who voted up on the candidate answer and all voters who voted
   * down on the candidate answer.
   * 
   * @param candidateAnswer - a candidate answer to a user's query.
   */
  @Override public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException {
    StackExchangeThread candidateAnswerThread = (StackExchangeThread) candidateAnswer;
    Set<StackExchangeAnswer> answers = candidateAnswerThread.getAnswers();
    Map<Integer, User> idToUserMap = buildIdToUserMap(answers);
    Map<VoteType, Set<Vote>> votes = candidateAnswerThread.getVoteMap();

    long upVotesUsersReputations = countUpVotersReputation(idToUserMap, votes.get(VoteType.UP_MOD));
    long downVotesUsersReputations = countDownVotersReputation(idToUserMap, votes.get(VoteType.DOWN_MOD));

    double score = transformScore(VotesRatioUtil.calculateVoteRatio(upVotesUsersReputations, downVotesUsersReputations,
        AnswerScorerConstants.VOTE_RATIO_FACTOR_VOTERS_REPUTATION));
    candidateAnswer.setFeatureValue(FeatureScorerEnums.VOTERS_REPUTATION_FEATURE_SCORER.toString(), score);
  }

  /**
   * Currently using Users of answers because no Users are associated with Votes (08/03/17)
   * 
   * @param answers
   * @return
   */
  private Map<Integer, User> buildIdToUserMap(Set<StackExchangeAnswer> answers) {
    Map<Integer, User> idToUserMap = new HashMap<>();
    for (StackExchangeAnswer answer : answers) {
      idToUserMap.put(answer.getAuthor().getId(), answer.getAuthor());
    }
    return idToUserMap;
  }

  private long countUpVotersReputation(Map<Integer, User> idToUserMap, Set<Vote> upVotes) {
    long upVotesUsersReputations = 0;
    for (Vote upVote : upVotes) {
      User voter = idToUserMap.get(upVote.getUserId());
      upVotesUsersReputations += voter.getReputation();
    }
    return upVotesUsersReputations;
  }

  private long countDownVotersReputation(Map<Integer, User> idToUserMap, Set<Vote> downVotes) {

    long downVotesUsersReputations = 0;
    for (Vote downVote : downVotes) {
      User voter = idToUserMap.get(downVote.getUserId());
      downVotesUsersReputations += voter.getReputation();
    }
    return downVotesUsersReputations;
  }

  /**
   * @param score - a score of a answer scorer feature
   * @return the provided score transformed in some way.
   */
  @Override public double transformScore(double score) {
    return score;
  }

}
