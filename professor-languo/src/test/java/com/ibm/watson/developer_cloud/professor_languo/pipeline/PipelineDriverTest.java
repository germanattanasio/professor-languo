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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerGenerator;
import com.ibm.watson.developer_cloud.professor_languo.api.PipelineQuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants.PipelineResultsTsvFileFormats;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineDriver;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.TrustingMergerAndRanker;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.evaluation.ResultWriter;

/**
 * The class <code>PipelineDriverTest</code> contains tests for the class {@link PipelineDriver}
 *
 */
public class PipelineDriverTest {

  PipelineDriverTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private static File dupThreadTsvFile =
      new File(PipelineDriverTest.class.getResource("/duplicateThreads/dup_thread.tsv").getPath());

  private static File serializedThreadsDir =
      new File(PipelineDriverTest.class.getResource("/duplicateThreads/").getPath());

  private Properties properties;

  @Rule public TemporaryFolder testOutputFolder = new TemporaryFolder();

  File resultOutputFile;

  @Test public void test_that_pipeline_can_be_executed_successfully() throws PipelineException, IOException {
    GIVEN.properties_are_initialized();
    WHEN.pipeline_is_launched();
    THEN.test_results_are_written_to_file();
  }

  private void properties_are_initialized() throws IOException {
    properties = new Properties();

    // QuestionSetManager properties
    properties.setProperty(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, dupThreadTsvFile.getAbsolutePath());
    properties.setProperty(ConfigurationConstants.QUESTION_SET_MANAGER_PARTITION_FRACTIONS, "[0.7, 0.3, 0.0]");

    // PipelineDriver properties
    properties.setProperty(ConfigurationConstants.PIPELINE_QUESTION_ANSWERER, DummyQuestionAnswerer.class.getName());
    properties.setProperty(PipelineQuestionAnswerer.PROP_NUM_THREADS, "1");
    properties.setProperty(PipelineQuestionAnswerer.PROP_NUM_FOLDS, "0");

    // Merger and Ranker properties
    properties.setProperty(TrustingMergerAndRanker.PROP_FEATURE_NAME, DummyQuestionAnswerer.DUMMY_FEATURE_NAME);

    // ResultWriter properties
    resultOutputFile = testOutputFolder.newFile();
    properties.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_PATH, resultOutputFile.getAbsolutePath());
    properties.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT,
        PipelineResultsTsvFileFormats.DEFAULT.toString());
  }

  private void pipeline_is_launched() throws PipelineException {
    PipelineDriver.drive(properties);
  }

  private void test_results_are_written_to_file() throws IOException {
    assertTrue(ResultWriter.class.getSimpleName() + " output file is missing", resultOutputFile.exists());
    assertEquals("Test results file contains incorrect number of rows",
        31 * com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineDriverTest.DummyQuestionAnswerer.DummyAnswerGenerator.NUM_ANSWERS
            + 1,
        FileUtils.readLines(resultOutputFile).size());
  }

  private static class DummyQuestionAnswerer extends PipelineQuestionAnswerer {

    private static final String DUMMY_FEATURE_NAME = "dummyFeature";

    @SuppressWarnings("unused") public DummyQuestionAnswerer() {
      super();
    }

    @Override public void initialize(Properties properties) {

      // Add the pipeline components for this dummy pipeline
      addAnswerGenerationComponent(new DummyAnswerGenerator());
      addAnswerMergerAndRankerComponent(new TrustingMergerAndRanker());
      addAnswerPostprocessorComponent(new ResultWriter());

      super.initialize(properties);
    }

    private static class DummyAnswerGenerator implements AnswerGenerator {

      private List<File> serializedThreadList;

      private Random rng = new Random();

      public static final int NUM_ANSWERS = 4;

      private int numThreads;

      @Override public void initialize(Properties properties) {
        serializedThreadList = new ArrayList<>(FileUtils.listFiles(serializedThreadsDir, new String[] {"ser"}, false));
        numThreads = serializedThreadList.size();
      }

      @Override public Observable<CandidateAnswer> generateCandidateAnswers(Question question) {
        // Randomly generate numAnswers different answers by
        // deserializing threads
        List<CandidateAnswer> candidateAnswers = new ArrayList<>();
        System.out.println(serializedThreadsDir);
        System.out.println(numThreads);
        int oldIndex = -1, currentIndex = rng.nextInt(numThreads);
        for (int i = 0; i < NUM_ANSWERS; i++) {
          while (currentIndex == oldIndex)
            currentIndex = rng.nextInt(numThreads);

          try {
            StackExchangeThread answer = StackExchangeThreadSerializer
                .deserializeThreadFromBinFile(serializedThreadList.get(currentIndex).getAbsolutePath());
            
            System.out.println(serializedThreadList.get(currentIndex).getAbsolutePath());
            
            answer.setFeatureValue(DUMMY_FEATURE_NAME, rng.nextDouble());
            candidateAnswers.add(answer);
          } catch (IngestionException e) {
            throw new RuntimeException(e);
          }

          oldIndex = currentIndex;
        }

        return Observable.from(candidateAnswers);
      }

    }
  }
}
