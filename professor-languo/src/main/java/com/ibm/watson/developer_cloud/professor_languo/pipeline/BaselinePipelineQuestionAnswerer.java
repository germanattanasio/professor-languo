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

import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerMergerAndRanker;
import com.ibm.watson.developer_cloud.professor_languo.api.AnswerPostprocessor;
import com.ibm.watson.developer_cloud.professor_languo.api.PipelineQuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.answer_gen.StackExchangeThreadAnswerGenerator;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.evaluation.ResultWriter;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.SingletonAnalyzer;

/**
 * The goal of this task is to extend the PipelineQuestionAnswerer abstract class to allow us to
 * execute a search-only pipeline. The major components of this pipeline are: 1) The search-based
 * answer generation component 2) A TrustingMergerAndRanker that outputs the Lucene score feature as
 * the final confidence 3) A ResultWriter that will write the pipeline results out to a flat file.
 * 
 */
public class BaselinePipelineQuestionAnswerer extends PipelineQuestionAnswerer {
  public void initialize(Properties properties) {
    // addQuestionAnalysisComponent(new QuestionAnalysis()); //we don't need
    // question analysis
    // component at this time

    SingletonAnalyzer.generateAnalyzer(properties.getProperty(ConfigurationConstants.ANALYZER));

    try {
      StackExchangeThreadAnswerGenerator candidateAnswerGenerator = new StackExchangeThreadAnswerGenerator();
      addAnswerGenerationComponent(candidateAnswerGenerator);
    } catch (SearchException e) {
      throw new RuntimeException(e);
    }

    AnswerMergerAndRanker answerMergerRanker = new TrustingMergerAndRanker();
    addAnswerMergerAndRankerComponent(answerMergerRanker);
    AnswerPostprocessor resultWriter = new ResultWriter();
    addAnswerPostprocessorComponent(resultWriter);

    super.initialize(properties);
  }

}
