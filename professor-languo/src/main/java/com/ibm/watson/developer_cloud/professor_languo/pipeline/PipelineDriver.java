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

import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Action1;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

import com.ibm.watson.developer_cloud.professor_languo.api.PipelineQuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.api.QuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.api.QuestionAnswerer.AnsweredQuestion;
import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;

/**
 * This class is used to run training and test sets of {@link StackExchangeQuestion
 * StackExchangeQuestions} through a chosen pipeline and evaluate performance.
 *
 */
public class PipelineDriver {

  private final static Logger logger = LogManager.getLogger(PipelineDriver.class.getName());

  public static void main(String[] args) {
    Properties properties = new Properties();
    try (FileInputStream propertiesFileStream = new FileInputStream(args[0])) {
      // Load the properties from the properties file
      properties.load(propertiesFileStream);

      // Load the bluemix properties
      IndexerAndSearcherFactory.loadStaticBluemixProperties(properties);

      // Launch the pipeline
      drive(properties);
    } catch (IOException | PipelineException e) {
      logger.fatal(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * This is the primary method that is used to:<br>
   * 1) Create training, validation, and test {@link QuestionAnswerSet} instances from a collection
   * of duplicate thread {@link StackExchangeQuestion} instances<br>
   * 2) Initialize a new {@link PipelineQuestionAnswerer}<br>
   * 3) Run the training and validation datasets through the pipeline<br>
   * 4) Run the test dataset through the pipeline
   * 
   * @param properties - {@link Properties} that govern the overall pipeline configuration
   * @throws PipelineException
   */
  public static void drive(Properties properties) throws PipelineException {
    long startTime = System.currentTimeMillis();
    RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
      @Override public void handleError(Throwable e) {
        throw new RuntimeException(e);
      }
    });

    // First, create a new QuestionSetManager, which will provide us with
    // the train, test,
    // and validation datasets
    QuestionSetManager questionSetManager = QuestionSetManager.newInstance(properties);

    // Next, get the train, test, and validation datasets
    QuestionAnswerSet trainingSet = questionSetManager.getTrainingSet();
    if (trainingSet != null) {
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.TRAINING_SET_NUMBERS"), //$NON-NLS-1$
          trainingSet.size(), trainingSet.getAnswers().size()));
    }

    QuestionAnswerSet validationSet = questionSetManager.getValidationSet();
    if (validationSet != null) {
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.VALIDATION_SET_NUMBERS"), //$NON-NLS-1$
          validationSet.size(), validationSet.getAnswers().size()));
    }

    QuestionAnswerSet testSet = questionSetManager.getTestSet();
    if (testSet != null) {
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.TEST_SET_NUMBERS"), //$NON-NLS-1$
          testSet.size(), testSet.getAnswers().size()));
    }

    // Next, instantiate and initialize the pipeline that we will be using
    QuestionAnswerer pipeline = PipelineFactory.newPipeline(properties);
    pipeline.initialize(properties);

    // Train the pipeline
    if (trainingSet != null)
      pipeline.train(trainingSet, validationSet);

    // Run the test set through the pipeline
    Observable<AnsweredQuestion> answeredQuestions = null;
    if (testSet != null) {
      answeredQuestions = pipeline.apply(testSet);
      answeredQuestions.toBlocking().forEach(new Action1<AnsweredQuestion>() {
        @Override public void call(AnsweredQuestion aq) {
          logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.TEST_FINISHED_QUESTION"), //$NON-NLS-1$
              aq.getQuestion().getId())); // )
        }
      });
    }

    // Terminate the pipeline
    pipeline.finish();

    long totalTime = System.currentTimeMillis() - startTime;
    logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.TOTAL_ELAPSED_TIME"), //$NON-NLS-1$
        PipelineDriver.class.getSimpleName(), DurationFormatUtils.formatDurationWords(totalTime, true, false)));
  }
}
