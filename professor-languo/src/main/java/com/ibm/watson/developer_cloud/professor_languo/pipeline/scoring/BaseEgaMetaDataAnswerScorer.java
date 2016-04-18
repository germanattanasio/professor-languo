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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerScorer;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.AnswerScorerConstants.FeatureScorerEnums;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components.TagSimilarityFeatureScorer;

public class BaseEgaMetaDataAnswerScorer implements AnswerScorer {

  List<EgaMetaDataScorerComponent> egaMetaDataScorerComponents = null;

  @Override public void initialize(Properties properties) {
    this.egaMetaDataScorerComponents = new ArrayList<EgaMetaDataScorerComponent>();

    String[] egaMetaDataFeatureScorers =
        properties.getProperty(ConfigurationConstants.EGA_METADATA_FEATURE_SCORERS).split(",");
    for (String featureScorerName : egaMetaDataFeatureScorers) {
      FeatureScorerEnums scorerNameEnum = FeatureScorerEnums.valueOf(featureScorerName);
      EgaMetaDataScorerComponent featureScorer;

      try {
        featureScorer = EgaMetaDataScorerComponentFactory.generateMetaDataScorerComponent(scorerNameEnum);
        this.egaMetaDataScorerComponents.add(featureScorer);
      } catch (AnswerScorerException e) {
        throw new RuntimeException(e);
      }
    }

  }

  /**
   * Calculates the score of each feature in the configuration for a provided candidate answer in
   * respect to the provided user question
   * 
   * @param question a question provided by the user
   * @param @candidateAnswer
   * @return a list of
   */
  @Override public CandidateAnswer scoreCandidateAnswer(Question question, CandidateAnswer answer) {

    for (EgaMetaDataScorerComponent featureScorer : egaMetaDataScorerComponents) {
      try {
        featureScorer.generateFeatureScore(answer);
        // This scorer is special because it needs the Question to score
        // the CandidateAnswer while others just need the
        // CandidateAnswer
        if (featureScorer.getClass().equals(TagSimilarityFeatureScorer.class)) {
          ((TagSimilarityFeatureScorer) featureScorer).generateFeatureScore(answer, question);
        }
      } catch (AnswerScorerException e) {
        throw new RuntimeException(e);
      }
    }
    return answer;

  }

}
