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

/**
 * This interface defines a primary search component that is responsible for producing a query
 * string that will be consumed by an {@link Searcher}
 *
 */
public interface QueryComponent {

  /**
   * Initialize the {@link QueryComponent}
   *
   * @param properties - A set of Java {@link Properties}
   */
  public void initialize(Properties properties) throws SearchException;

  /**
   * Produce a query string that will be consumed by a {@link Searcher}
   *
   * @param question - The {@link Question} being asked
   * @return A <code>Query</code> that will be consumed by a {@link Searcher}
   * @throws SearchException
   */
  public Query generateQuery(Question question) throws SearchException;

}
