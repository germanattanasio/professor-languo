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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.api.QuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineFactory;

/**
 * The class <code>PipelineFactoryTest</code> contains tests for the class {@link PipelineFactory}
 */
public class PipelineFactoryTest {

  PipelineFactoryTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private Properties properties;

  private QuestionAnswerer pipeline;

  @Test public void test_that_a_new_question_answerer_can_be_created() throws PipelineException {
    GIVEN.properties_are_initialized();
    WHEN.pipeline_is_created();
    THEN.created_pipeline_is_of_expected_class();
  }

  @Test(expected = PipelineException.class) public void test_that_a_missing_property_throws_exception()
      throws PipelineException {
    GIVEN.properties_are_initialized_with_missing_parameter();
    WHEN.pipeline_is_created();
  }

  @Test(expected = PipelineException.class) public void test_that_instantiating_an_invalid_pipeline_throws_exception()
      throws PipelineException {
    GIVEN.properties_are_initialized_with_invalid_pipeline_class();
    WHEN.pipeline_is_created();
  }

  private void properties_are_initialized() {
    properties = new Properties();
    properties.setProperty(ConfigurationConstants.PIPELINE_QUESTION_ANSWERER, DummyQuestionAnswerer.class.getName());
  }

  private void pipeline_is_created() throws PipelineException {
    pipeline = PipelineFactory.newPipeline(properties);
  }

  private void created_pipeline_is_of_expected_class() {
    assertEquals("Actual pipeline class does not match expected", DummyQuestionAnswerer.class, pipeline.getClass());
  }

  private void properties_are_initialized_with_missing_parameter() {
    properties = new Properties();
  }

  private void properties_are_initialized_with_invalid_pipeline_class() {
    properties = new Properties();
    properties.setProperty(ConfigurationConstants.PIPELINE_QUESTION_ANSWERER, Integer.class.getName());
  }

  private static class DummyQuestionAnswerer implements QuestionAnswerer {

    @SuppressWarnings("unused") public DummyQuestionAnswerer() {
      super();
    }

    @Override public void initialize(Properties properties) {
      return;
    }

    @Override public void train(QuestionAnswerSet trainSet, QuestionAnswerSet componentTraining) {
      return;
    }

    @Override public Observable<AnsweredQuestion> apply(QuestionAnswerSet testSet) {
      return null;
    }

    @Override public Observable<List<CandidateAnswer>> answer(Question question,
        Collection<CorrectAnswer> correctAnswers) {
      return null;
    }

    @Override public void finish() {
      return;
    }

  }
}
