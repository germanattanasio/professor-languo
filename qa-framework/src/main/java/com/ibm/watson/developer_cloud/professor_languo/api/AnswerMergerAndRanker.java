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

import java.util.Collection;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;

public interface AnswerMergerAndRanker extends QAComponent {

  /**
   * Merges and ranks existing candidate answers.
   * 
   * @param question the question to be answered
   * @param answer a candidate answer to be scored
   * @param correctAnswers If the merger/ranker is being trained this will be a set of correct
   *        answers for use as positive training labels. If the merger/ranker is being applied to a
   *        test set, this will be null.
   */
  public Observable<CandidateAnswer> mergeAndRankAnswers(Question question, Observable<CandidateAnswer> answers,
      Collection<CorrectAnswer> correctAnswers);

  /**
   * This method is called once during the training sequence after mergeAndRankAnswers has been
   * called once per question on all questions in the train set.
   */
  public void finishTraining();

}
