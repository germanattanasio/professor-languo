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
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.TagSimilarityFeatureScorer;

public class TagSimilarityFeatureScorerTest extends BaseEgaAnswerScorerTests {
  TagSimilarityFeatureScorerTest GIVEN = this, WHEN = this, THEN = this, OR = this, AND = this;
  double refScore1, refScore2, refScore3;
  double score1, score2, score3;

  @Test public void test_that_mutual_tag_counts_work() throws AnswerScorerException {
    // START QUESTION 1
    GIVEN.sample_input_is_created();
    WHEN.feature_score_pair_produced();
    AND.reference_feature_score_produced();
    THEN.feature_score_should_match_reference_feature_score();
  }

  private void feature_score_pair_produced() throws AnswerScorerException {
    TagSimilarityFeatureScorer scorer = new TagSimilarityFeatureScorer();
    scorer.generateFeatureScore(candidateAnswers[0], question1);
    scorer.generateFeatureScore(candidateAnswers[1], question1);
    scorer.generateFeatureScore(candidateAnswers[2], question1);
    score1 = candidateAnswers[0].getFeatureValue(FeatureScorerEnums.TAG_SIMILARITY_FEATURE_SCORER.toString());
    score2 = candidateAnswers[1].getFeatureValue(FeatureScorerEnums.TAG_SIMILARITY_FEATURE_SCORER.toString());
    score3 = candidateAnswers[2].getFeatureValue(FeatureScorerEnums.TAG_SIMILARITY_FEATURE_SCORER.toString());
  }

  private void reference_feature_score_produced() throws AnswerScorerException {
    refScore1 = 3.0;
    refScore2 = 2.0;
    refScore3 = 0.0;
  }

  private void feature_score_should_match_reference_feature_score() {
    assertTrue("Feature score for answer 1 does not match", Double.compare(score1, refScore1) == 0);
    assertTrue("Feature score for answer 2 does not match", Double.compare(score2, refScore2) == 0);
    assertTrue("Feature score for answer 3 does not match", Double.compare(score3, refScore3) == 0);
  }
}
