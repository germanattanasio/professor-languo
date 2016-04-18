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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search;

import java.util.Properties;

import org.apache.lucene.search.Query;

import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;

/**
 * BoostStandardQueryGenerator generates a boost standard query for the {@link Question}. The boost
 * standard query retrieves the Lucene Documents whose {@link IndexDocumentFieldName#THREAD_TITLE}
 * {@link IndexDocumentFieldName#THREAD_TEXT} {@link IndexDocumentFieldName#THREAD_TAGS}
 * {@link IndexDocumentFieldName#ACCEPTED_ANSWER_TEXT}
 * {@link IndexDocumentFieldName#TOP_VOTED_ANSWER_TEXT} field contains any unigram token of the
 * {@link Question}'s title field. The {@link IndexDocumentFieldName#THREAD_TITLE} and
 * {@link IndexDocumentFieldName#THREAD_TAGS} are more important than other fields so they have
 * higher boost than others.
 * 
 */
public class BoostStandardQueryGenerator extends BoostQueryGenerator {

  @Override public void initialize(Properties properties) throws SearchException {}

  @Override public Query generateQuery(Question question) throws SearchException {
    return createBoostStandardQuery(question);
  }

}
