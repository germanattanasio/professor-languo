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

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerPostprocessor;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants.PipelineResultsTsvFileFormats;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;

/**
 * This {@link AnswerPostprocessor} is used to write out the results of running a set of questions
 * (a {@link QuestionAnswerSet}) through a pipeline. The output is a tab-separated value (TSV) file
 * whose format is determined by the {@link ConfigurationConstants#PIPELINE_RESULTS_TSV_FILE_FORMAT
 * PIPELINE_RESULTS_TSV_FILE_FORMAT} parameter, and whose path is determined by
 * {@link ConfigurationConstants#PIPELINE_RESULTS_TSV_FILE_PATH PIPELINE_RESULTS_TSV_FILE_PATH}.
 * 
 */
public class ResultWriter implements AnswerPostprocessor {

  private CSVPrinter writer;
  private PipelineResultsTsvFileFormats format;

  public ResultWriter() {
    super();
  }

  @Override public void initialize(Properties properties) {
    // Retrieve necessary properties
    String resultsFilePath = properties.getProperty(ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_PATH);
    this.format = PipelineResultsTsvFileFormats.valueOf(properties.getProperty(
        ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT, PipelineResultsTsvFileFormats.DEFAULT.toString()));

    // Make sure file path was actually specified
    if (resultsFilePath == null)
      throw new RuntimeException(MessageFormat.format(Messages.getString("RetrieveAndRank.MISSING_PROPERTY"), //$NON-NLS-1$
          ConfigurationConstants.PIPELINE_RESULTS_TSV_FILE_FORMAT));

    // Open a FileWriter, using CSV or TSV format depending on desired
    // output format
    try {
      writer = (this.format == PipelineResultsTsvFileFormats.COMPETITION)
          ? new CSVPrinter(new FileWriter(resultsFilePath), CSVFormat.DEFAULT)
          : new CSVPrinter(new FileWriter(resultsFilePath), CSVFormat.TDF.withHeader(getHeaders(this.format)));
    } catch (IOException e) {
      throw new RuntimeException(new PipelineException(e));
    }
  }

  @Override public Observable<CandidateAnswer> postprocessAnswers(Question question,
      Observable<CandidateAnswer> answers, Collection<CorrectAnswer> correctAnswers) {

    if (question instanceof StackExchangeQuestion) {
      // Write out all of the answers to this question to the TSV file
      // according to the desired format
      try {
        switch (this.format) {
          case DEFAULT:
            for (CandidateAnswer answer : answers.toBlocking().toIterable()) {
              writer.printRecord(question.getId(), answer.getAnswerLabel(), answer.getConfidence(),
                  CorrectAnswer.isCorrect(answer, correctAnswers));
            }
            break;

          case VERBOSE:
            for (CandidateAnswer answer : answers.toBlocking().toIterable()) {
              if (answer instanceof StackExchangeThread) {
                writer.printRecord(question.getId(), ((StackExchangeQuestion) question).getTitleText(),
                    answer.getAnswerLabel(), ((StackExchangeThread) answer).getQuestion().getTitle(),
                    answer.getConfidence(), CorrectAnswer.isCorrect(answer, correctAnswers));
              }
            }
            break;

          case COMPETITION:
            ArrayList<String> record = new ArrayList<>();
            record.add(question.getId());
            for (CandidateAnswer answer : answers.toBlocking().toIterable()) {
              record.add(answer.getAnswerLabel());
              record.add(Double.toString(answer.getConfidence()));
            }
            writer.printRecord(record);
            break;
        }
      } catch (IOException e) {
        throw new RuntimeException(new PipelineException(e));
      }
    } else {
      throw new RuntimeException(
          new PipelineException(MessageFormat.format(Messages.getString("RetrieveAndRank.QUESTION_TYPES"), //$NON-NLS-1$
              ResultWriter.class.getSimpleName(), StackExchangeQuestion.class.getSimpleName())));
    }

    // We haven't made any changes to the answers, thus return as-is
    return answers;
  }

  @Override public void finishPostprocessing() {
    try {
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(new PipelineException(e));
    }
  }

  /**
   * @param format - The {@link PipelineResultsTsvFileFormats} format
   * @return A String array of column headers
   */
  private String[] getHeaders(PipelineResultsTsvFileFormats format) {
    switch (format) {
      case DEFAULT:
        return new String[] {"QID", "AnswerLabel", "Confidence", "IsCorrect"};

      case VERBOSE:
        return new String[] {"QID", "QuestionTitle", "AnswerLabel", "AnswerTitle", "Confidence", "IsCorrect"};

      case COMPETITION:
        return null;

      default:
        return null;
    }
  }

}
