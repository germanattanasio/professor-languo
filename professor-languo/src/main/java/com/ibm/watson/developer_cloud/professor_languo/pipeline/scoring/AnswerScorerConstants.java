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

public class AnswerScorerConstants {
  public static enum FeatureScorerEnums {
    AUTHOR_REPUTATION_FEATURE_SCORER, AGE_FACTOR_FEATURE_SCORER, ALL_ANSWERS_VOTE_RATIO_FEATURE_SCORER, ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER, NUMBER_OF_ANSWERS_FEATURE_SCORER, PAGE_VIEWS_FEATURE_SCORER, VOTE_RATIO_FEATURE_SCORER, VOTERS_REPUTATION_FEATURE_SCORER, TAG_SIMILARITY_FEATURE_SCORER
  }

  public static final int VOTE_RATIO_FACTOR_CANDIDATE_ANSWER = 20;
  public static final int VOTE_RATIO_FACTOR_VOTERS_REPUTATION = 25;
  public static final int VOTE_RATIO_FACTOR_ACCEPTED_ANSWER_VOTES = 18;
  public static final int VOTE_RATIO_FACTOR_ALL_ANSWERS_VOTES = 22;

}
