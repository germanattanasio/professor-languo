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

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;

/**
 * An answer generator takes an analyzed question and returns a set of answers, typically with one
 * or more features already assigned values. For example, one answer generator might search through
 * a set of documents and assign features indicating the search score and search rank.
 * 
 */
public interface AnswerGenerator extends QAComponent {

  /**
   * Identify the answers to the specified question. This method will return a set of possible
   * answers with feature values. The returned answers do not have to be in any particular order.
   * <p>
   * The number of returned answers should be from none to the number of answers in the training
   * set. If there is no answer to the question, an empty map should be returned.
   * 
   * @param question the question to be answered
   * @return a map that maps possible answers with their confidence values between 0 and 1.
   */
  public Observable<CandidateAnswer> generateCandidateAnswers(Question question);

}
