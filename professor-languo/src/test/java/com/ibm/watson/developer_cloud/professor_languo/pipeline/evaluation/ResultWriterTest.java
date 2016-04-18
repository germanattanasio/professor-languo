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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.evaluation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants.PipelineResultsTsvFileFormats;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Post;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.evaluation.ResultWriter;

public class ResultWriterTest {

  ResultWriterTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private ResultWriter resultWriter;

  private StackExchangeQuestion question1, question2;
  private Observable<CandidateAnswer> answers1, answers2;
  private Collection<CorrectAnswer> correctAnswers1, correctAnswers2;

  @Rule public TemporaryFolder testOutputFolder = new TemporaryFolder();

  private File resultOutputFile;

  @Test public void test_that_default_format_results_are_written_correctly() throws IOException {
    GIVEN.sample_input_is_created();
    AND.default_result_writer_is_initialized();
    WHEN.results_are_written();
    AND.post_processing_finishes();
    THEN.default_results_match_reference_default_results();
  }

  @Test public void test_that_verbose_format_results_are_written_correctly() throws IOException {
    GIVEN.sample_input_is_created();
    AND.verbose_result_writer_is_initialized();
    WHEN.results_are_written();
    AND.post_processing_finishes();
    THEN.verbose_results_match_reference_verbose_results();
  }

  @Test public void test_that_competition_format_results_are_written_correctly() throws IOException {
    GIVEN.sample_input_is_created();
    AND.competition_result_writer_is_initialized();
    WHEN.results_are_written();
    AND.post_processing_finishes();
    THEN.competition_results_match_reference_competition_results();
  }

  private void sample_input_is_created() {
    question1 = new StackExchangeQuestion(
        "What is the Answer to the Ultimate Question of Life" + ", the Universe, and Everything?", null, null, 42, "");

    Post answer1QuestionPost = new Post();
    answer1QuestionPost.setId(1);
    answer1QuestionPost.setTitle("Eat cheese!");
    StackExchangeThread answer1 = new StackExchangeThread(answer1QuestionPost, null, null, null);
    answer1.setConfidence(1.0);

    Post answer2QuestionPost = new Post();
    answer2QuestionPost.setId(2);
    answer2QuestionPost.setTitle("Be kind");
    StackExchangeThread answer2 = new StackExchangeThread(answer2QuestionPost, null, null, null);
    answer2.setConfidence(0.8);

    Post answer3QuestionPost = new Post();
    answer3QuestionPost.setId(3);
    answer3QuestionPost.setTitle("Have courage");
    StackExchangeThread answer3 = new StackExchangeThread(answer3QuestionPost, null, null, null);
    answer3.setConfidence(0.75);

    answers1 = Observable.from(new CandidateAnswer[] {answer1, answer2, answer3});

    CorrectAnswer correctAnswer = new CorrectAnswer("1");
    correctAnswers1 = Arrays.asList(correctAnswer);

    question2 = new StackExchangeQuestion("Who stole the cookie from the cookie jar?", null, null, 6, "");

    answer1QuestionPost = new Post();
    answer1QuestionPost.setId(7);
    answer1QuestionPost.setTitle("Not Justin.  No way.");
    answer1 = new StackExchangeThread(answer1QuestionPost, null, null, null);
    answer1.setConfidence(0.95);

    answer2QuestionPost = new Post();
    answer2QuestionPost.setId(8);
    answer2QuestionPost.setTitle("Definitely Justin.  What a jerk!");
    answer2 = new StackExchangeThread(answer2QuestionPost, null, null, null);
    answer2.setConfidence(0.5);

    answer3QuestionPost = new Post();
    answer3QuestionPost.setId(9);
    answer3QuestionPost.setTitle("Probably Justin, but we can't be sure...");
    answer3 = new StackExchangeThread(answer3QuestionPost, null, null, null);
    answer3.setConfidence(0.25);

    answers2 = Observable.from(new CandidateAnswer[] {answer1, answer2, answer3});

    correctAnswer = new CorrectAnswer("8");
    correctAnswers2 = Arrays.asList(correctAnswer);
  }

  private void default_result_writer_is_initialized() throws IOException {
    resultOutputFile = testOutputFolder.newFile();

    Properties props = new Properties();
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_PATH, resultOutputFile.getAbsolutePath());
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT,
        PipelineResultsTsvFileFormats.DEFAULT.toString());

    resultWriter = new ResultWriter();
    resultWriter.initialize(props);
  }

  private void results_are_written() {
    resultWriter.postprocessAnswers(question1, answers1, correctAnswers1);
    resultWriter.postprocessAnswers(question2, answers2, correctAnswers2);
  }

  private void post_processing_finishes() {
    resultWriter.finishPostprocessing();
  }

  private void default_results_match_reference_default_results() throws IOException {
    File defaultReferenceFile =
        new File(ResultWriterTest.class.getResource("/resultWriter/defaultFormatOutput.csv").getPath());
    assertEquals("Default format output does not match reference output",
        FileUtils.readFileToString(defaultReferenceFile), FileUtils.readFileToString(resultOutputFile));
  }

  private void verbose_result_writer_is_initialized() throws IOException {
    resultOutputFile = testOutputFolder.newFile();

    Properties props = new Properties();
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_PATH, resultOutputFile.getAbsolutePath());
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT,
        PipelineResultsTsvFileFormats.VERBOSE.toString());

    resultWriter = new ResultWriter();
    resultWriter.initialize(props);
  }

  private void verbose_results_match_reference_verbose_results() throws IOException {
    File defaultReferenceFile =
        new File(ResultWriterTest.class.getResource("/resultWriter/verboseFormatOutput.csv").getPath());
    assertEquals("Default format output does not match reference output",
        FileUtils.readFileToString(defaultReferenceFile), FileUtils.readFileToString(resultOutputFile));
  }

  private void competition_result_writer_is_initialized() throws IOException {
    resultOutputFile = testOutputFolder.newFile();

    Properties props = new Properties();
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_PATH, resultOutputFile.getAbsolutePath());
    props.setProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT,
        PipelineResultsTsvFileFormats.COMPETITION.toString());

    resultWriter = new ResultWriter();
    resultWriter.initialize(props);
  }

  private void competition_results_match_reference_competition_results() throws IOException {
    File defaultReferenceFile =
        new File(ResultWriterTest.class.getResource("/resultWriter/competitionFormatOutput.csv").getPath());
    System.out.println(FileUtils.readFileToString(resultOutputFile));
    System.out.println(FileUtils.readFileToString(defaultReferenceFile));

    assertEquals("Default format output does not match reference output",
        FileUtils.readFileToString(defaultReferenceFile), FileUtils.readFileToString(resultOutputFile));
  }
}
