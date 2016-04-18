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

import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.AnswerScorerException;

public interface EgaMetaDataScorerComponent {
  public void initialize(Properties properties) throws AnswerScorerException;

  public void generateFeatureScore(CandidateAnswer candidateAnswer) throws AnswerScorerException;

  /**
   * To get the best performance, feature values should be transformed. All scorers must implement
   * their own means of transforming their scores.
   * 
   * Reason being that if you think of pageViews as a feature, log(pageViews) may be better than
   * pageViews itself because the difference between 1 and 100 pageViews may be more significant, in
   * some cases, than the difference between 10 million and 100 million pageViews.
   * 
   * @param score the score, value, of the feature to be transformed.
   * @return the transformed score
   */
  public abstract double transformScore(double score);
}
