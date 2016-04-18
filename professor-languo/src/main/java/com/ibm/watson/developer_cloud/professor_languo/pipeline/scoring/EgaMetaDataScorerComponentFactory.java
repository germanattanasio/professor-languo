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

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.AcceptedAnswerVoteRatioFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.AllAnswersVoteRatioFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.AuthorReputationFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.NumberOfAnswersFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.PageViewsFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.TagSimilarityFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.VoteRatioFeatureScorer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.VotersReputationRatioFeatureScorer;

public class EgaMetaDataScorerComponentFactory {

  public static EgaMetaDataScorerComponent generateMetaDataScorerComponent(
      AnswerScorerConstants.FeatureScorerEnums type) throws AnswerScorerException {
    EgaMetaDataScorerComponent metaDataScorerComponent = null;
    switch (type) {
      case AUTHOR_REPUTATION_FEATURE_SCORER:
        metaDataScorerComponent = new AuthorReputationFeatureScorer();
        break;
      case ALL_ANSWERS_VOTE_RATIO_FEATURE_SCORER:
        metaDataScorerComponent = new AllAnswersVoteRatioFeatureScorer();
        break;
      case ACCEPTED_ANSWER_VOTE_RATIO_FEATURE_SCORER:
        metaDataScorerComponent = new AcceptedAnswerVoteRatioFeatureScorer();
        break;
      case NUMBER_OF_ANSWERS_FEATURE_SCORER:
        metaDataScorerComponent = new NumberOfAnswersFeatureScorer();
        break;
      case PAGE_VIEWS_FEATURE_SCORER:
        metaDataScorerComponent = new PageViewsFeatureScorer();
        break;
      case VOTE_RATIO_FEATURE_SCORER:
        metaDataScorerComponent = new VoteRatioFeatureScorer();
        break;
      case VOTERS_REPUTATION_FEATURE_SCORER:
        metaDataScorerComponent = new VotersReputationRatioFeatureScorer();
        break;
      case TAG_SIMILARITY_FEATURE_SCORER:
        metaDataScorerComponent = new TagSimilarityFeatureScorer();
        break;
      default:
        metaDataScorerComponent = null;
        throw new IllegalArgumentException(Messages.getString("RetrieveAndRank.FEATURE_SCORER_NULL")); //$NON-NLS-1$
    }
    return metaDataScorerComponent;
  }
}
