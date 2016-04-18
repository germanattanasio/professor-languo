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

package com.ibm.watson.developer_cloud.professor_languo.pipeline;

import java.util.Collection;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Func1;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerMergerAndRanker;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;

public class TrustingMergerAndRanker implements AnswerMergerAndRanker {

  private static final Logger logger = LogManager.getLogger();
  public static final String PROP_FEATURE_NAME = "TrustingMergerAndRanker.featureName";
  private String trustedFeatureName;

  @Override public Observable<CandidateAnswer> mergeAndRankAnswers(Question question,
      Observable<CandidateAnswer> answers, Collection<CorrectAnswer> correctAnswers) {
    return answers.map(new Func1<CandidateAnswer, CandidateAnswer>() {
      @Override public CandidateAnswer call(CandidateAnswer answer) {
        Double score = answer.getFeatureValue(trustedFeatureName);
        if (score == null)
          score = 0d;
        answer.setConfidence(score);
        if (logger.isDebugEnabled())
          logger.debug(answer.getAnswerLabel() + " " + score);
        return answer;
      }

    });
  }

  @Override public void initialize(Properties properties) {
    this.trustedFeatureName = properties.getProperty(PROP_FEATURE_NAME);
  }

  @Override public void finishTraining() {
    // normally we would train a model using features we've observed in the
    // training data here.
    // However, this example merger/ranker is so simple that it doesn't need
    // to do anything
    // for this step.
  }
}
