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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;

public class NaiveFreeQueryGenerator implements QueryComponent {

  @Override public void initialize(Properties properties) throws SearchException {}

  /**
   * Create a simple query where each field of the question is a single term
   */
  @Override public Query generateQuery(Question question) throws SearchException {

    // All non-alphanumeric characters are removed, since they interfere
    // with REST calls
    StackExchangeQuestion stackQuestion = (StackExchangeQuestion) question;
    String title = stackQuestion.getTitleText().trim().replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit} ]", "");

    // Create a boolean query where we will add terms for the search
    BooleanQuery query = new BooleanQuery();

    // Add the search terms to the query
    // Each term should appear in the search but doesn't have to

    // title is used a free query with no field attached to it
    query.add(new TermQuery(new Term("", title)), BooleanClause.Occur.SHOULD);

    return query;
  }

}
