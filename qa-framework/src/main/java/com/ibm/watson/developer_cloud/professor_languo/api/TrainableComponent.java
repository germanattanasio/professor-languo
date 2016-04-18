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

package com.ibm.watson.developer_cloud.professor_languo.api;

import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;

/**
 * A component that requires training against a training set.
 * 
 */
public interface TrainableComponent extends QAComponent {

  /**
   * Train the classifier against the specified training set. A training set should be a
   * {@link QuestionAnswerSet} object that contains a set of questions with their answers. A
   * question can have more than one answer.
   * 
   * @param trainingSet the question answer pairs to be trained against
   * @see QuestionAnswerSet
   */
  public void train(QuestionAnswerSet trainingSet);

}
