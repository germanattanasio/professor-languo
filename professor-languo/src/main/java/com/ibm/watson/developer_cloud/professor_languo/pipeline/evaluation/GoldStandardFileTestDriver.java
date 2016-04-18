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

import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineDriver;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.QuestionSetManager;

public class GoldStandardFileTestDriver {
  private final static Logger logger = LogManager.getLogger(PipelineDriver.class.getName());

  public static void main(String[] args) {
    Properties properties = new Properties();
    try (FileInputStream propertiesFileStream = new FileInputStream(args[0])) {
      // Load the properties from the properties file
      properties.load(propertiesFileStream);

      // Launch the pipeline
      drive(properties);
    } catch (IOException | PipelineException e) {
      logger.fatal(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static void drive(Properties properties) throws PipelineException {
    // First, create a new QuestionSetManager, which will provide us with
    // the train, test,
    // and validation datasets
    QuestionSetManager questionSetManager = QuestionSetManager.newInstance(properties);

    QuestionAnswerSet testSet = questionSetManager.getTestSet();
    if (testSet != null) {
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.TEST_SET_NUMBERS"), //$NON-NLS-1$
          testSet.size(), testSet.getAnswers().size()));
    }

    // Then, generate the gold standard file based on the test set
    if (testSet != null) {
      String resultsFilePath = properties.getProperty(ConfigurationConstants.PIPELINE_GOLD_STANDARD_TSV_FILE_PATH);
      // Competitions.generateGoldStandardFile(resultsFilePath, testSet);
    }
  }
}
