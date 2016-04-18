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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.EgaMetaDataScorerComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.FeatureScorePair;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.AuthorReputationFeatureScorer;

public class AuthorReputationFeatureScorerTest extends BaseEgaAnswerScorerTests {
  AuthorReputationFeatureScorerTest GIVEN = this, WHEN = this, THEN = this, OR = this, AND = this;
  FeatureScorePair featureScorePair = null;
  FeatureScorePair referenceFeatureScorePair = null;

  @Test public void test_that_accepted_answer_vote_ratio_feature_scorer_works() throws AnswerScorerException {
    GIVEN.sample_input_is_created();
    WHEN.feature_score_pair_produced();
    AND.reference_feature_score_pair_produced();
    THEN.feature_score_pair_should_match_reference_feature_score_pair();
  }

  private void reference_feature_score_pair_produced() {
    // TODO Auto-generated method stub
    // StackExchangeThread answerThread = (StackExchangeThread)
    // candidateAnswers[0];
    double score = 10_000; // answerThread.getAuthor().getReputation();

    referenceFeatureScorePair = new FeatureScorePair(FeatureScorerEnums.AUTHOR_REPUTATION_FEATURE_SCORER.toString(),
        new AuthorReputationFeatureScorer().transformScore(score));
  }

  private void feature_score_pair_produced() throws AnswerScorerException {
    // TODO Auto-generated method stub
    EgaMetaDataScorerComponent metaDataScorerComponent = new AuthorReputationFeatureScorer();
    metaDataScorerComponent.generateFeatureScore(candidateAnswers[0]);
    featureScorePair = new FeatureScorePair(FeatureScorerEnums.AUTHOR_REPUTATION_FEATURE_SCORER.toString(),
        candidateAnswers[0].getFeatureValue(FeatureScorerEnums.AUTHOR_REPUTATION_FEATURE_SCORER.toString()));
  }

  private void feature_score_pair_should_match_reference_feature_score_pair() {
    // TODO Auto-generated method stub
    assertTrue("Feature Score Pair Value = Reference Feature Score Pair Key",
        featureScorePair.getKey().equals(referenceFeatureScorePair.getKey()));
    assertTrue("Feature Score Pair Value = Reference Feature Score Pair Value",
        Double.compare(featureScorePair.getValue(), referenceFeatureScorePair.getValue()) == 0);
  }
}
