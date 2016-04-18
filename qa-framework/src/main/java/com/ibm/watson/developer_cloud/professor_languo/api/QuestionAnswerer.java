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
import java.util.List;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;

import rx.Observable;

/**
 * A question answerer takes a question and provides a ranked list of answers with confidence
 * scores.
 * 
 */
public interface QuestionAnswerer extends QAComponent {

  /**
   * Trains the QuestionAnswer. This method should be called exactly once on a train set before any
   * calls to {@link #answer(TextWithAnalysis)} or {@link #test(QuestionAnswerSet)}.
   * 
   * @param trainSet The set used to train the entire pipeline.
   * @param componentTraining Null if the train set should also be used to train the subcomponents.
   *        Otherwise, a separate set for training subcomponents.
   */
  public void train(QuestionAnswerSet trainSet, QuestionAnswerSet componentTraining);

  /**
   * Answers a set of questions.
   * 
   * @param testSet Questions.
   * @return An observable with one element per question (but not necessarily in the same order).
   *         Each element has one of the questions and its answers.
   */
  public Observable<AnsweredQuestion> apply(final QuestionAnswerSet testSet);

  /**
   * Answers a single question.
   * 
   * @param question
   * @return An observable with one element: a ranked list of answers with confidence scores.
   */
  public Observable<List<CandidateAnswer>> answer(Question question, Collection<CorrectAnswer> correctAnswers);

  /**
   * This method is called once when the question answerer is done, to allow it to complete anything
   * that requires completion, e.g., closing open files.
   */
  public void finish();


  public class AnsweredQuestion {
    final Question question;
    final List<CandidateAnswer> answers;

    public AnsweredQuestion(Question question, List<CandidateAnswer> answers) {
      this.question = question;
      this.answers = answers;
    }

    public Question getQuestion() {
      return question;
    }

    public List<CandidateAnswer> getAnswers() {
      return answers;
    }
  }


}
