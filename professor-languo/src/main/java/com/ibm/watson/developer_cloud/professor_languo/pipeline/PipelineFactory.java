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

import java.text.MessageFormat;
import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.api.QuestionAnswerer;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;

/**
 * This factory class is used to create new {@link QuestionAnswerer} instances at runtime.
 */
public class PipelineFactory {

  /**
   * Create a new pipeline {@link QuestionAnswerer}
   * 
   * @param properties - A {@link Properties} object that contains the property
   *        {@link ConfigurationConstants#PIPELINE_QUESTION_ANSWERER}, indicating the specific
   *        pipeline to instantiate
   * @return A new {@link QuestionAnswerer} instance
   * @throws PipelineException
   */
  public static QuestionAnswerer newPipeline(Properties properties) throws PipelineException {
    String pipeline = properties.getProperty(ConfigurationConstants.PIPELINE_QUESTION_ANSWERER);
    if (pipeline == null)
      throw new PipelineException(MessageFormat.format(Messages.getString("RetrieveAndRank.MISSING_PROPERTY"), //$NON-NLS-1$
          ConfigurationConstants.PIPELINE_QUESTION_ANSWERER));
    else
      return produceQuestionAnswerer(pipeline);
  }

  /**
   * Instantiate a new {@link QuestionAnswerer} with the specified class name
   * 
   * @param className - The pipeline class to instantiate
   * @return A new {@link QuestionAnswerer} instance
   * @throws PipelineException
   */
  private static QuestionAnswerer produceQuestionAnswerer(String className) throws PipelineException {
    Class<? extends QuestionAnswerer> pipelineClass;
    try {
      pipelineClass = Class.forName(className).asSubclass(QuestionAnswerer.class);
    } catch (ClassNotFoundException | ClassCastException e) {
      throw new PipelineException(e);
    }

    QuestionAnswerer instance;

    try {
      instance = pipelineClass.newInstance();
    } catch (IllegalAccessException | InstantiationException e) {
      throw new PipelineException(e);
    }

    return instance;
  }
}
