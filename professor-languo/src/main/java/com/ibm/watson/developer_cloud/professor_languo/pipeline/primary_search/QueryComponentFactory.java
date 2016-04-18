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

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;

/**
 * {@link QueryComponentFactory} is used to generate different {@link QueryComponent} according to
 * the name of the {@link QueryComponent}
 *
 */
public class QueryComponentFactory {

  public static QueryComponent generateQueryComponent(String type) throws SearchException {
    QueryComponent queryComponent = null;
    switch (type) {
      case PrimarySearchConstants.NAIVE_STANDARD_QUERY_GENERATOR:
        queryComponent = new NaiveStandardQueryGenerator();
        break;
      case PrimarySearchConstants.NAIVE_BIGRAM_QUERY_GENERATOR:
        queryComponent = new NaiveBigramQueryGenerator();
        break;
      case PrimarySearchConstants.NAIVE_TRIGRAM_QUERY_GENERATOR:
        queryComponent = new NaiveTrigramQueryGenerator();
        break;
      case PrimarySearchConstants.NAIVE_FREE_QUERY_GENERATOR:
        queryComponent = new NaiveFreeQueryGenerator();
        break;
      case PrimarySearchConstants.BOOST_STANDARD_QUERY_GENERATOR:
        queryComponent = new BoostStandardQueryGenerator();
        break;
      case PrimarySearchConstants.BOOST_BIGRAM_QUERY_GENERATOR:
        queryComponent = new BoostBigramQueryGenerator();
        break;
      case PrimarySearchConstants.BOOST_TRIGRAM_QUERY_GENERATOR:
        queryComponent = new BoostTrigramQueryGenerator();
        break;
      case PrimarySearchConstants.BOOST_UNIGRAM_QUERY_GENERATOR:
        queryComponent = new BoostUnigramQueryGenerator();
        break;
      case PrimarySearchConstants.SPAN_BIGRAM_QUERY_GENERATOR:
        queryComponent = new SpanBigramQueryGenerator();
        break;
      case PrimarySearchConstants.SPAN_TRIGRAM_QUERY_GENERATOR:
        queryComponent = new SpanTrigramQueryGenerator();
        break;
      default:
        queryComponent = null;
        throw new IllegalArgumentException(Messages.getString("RetrieveAndRank.QUERY_GENERATOR_NOT_FOUND")); //$NON-NLS-1$
    }
    return queryComponent;
  }

}
