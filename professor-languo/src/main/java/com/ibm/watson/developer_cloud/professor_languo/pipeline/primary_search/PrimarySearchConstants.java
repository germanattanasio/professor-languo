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

public class PrimarySearchConstants {
  public static final String NAIVE_STANDARD_QUERY_GENERATOR = "NAIVE_STANDARD_QUERY_GENERATOR";
  public static final String NAIVE_BIGRAM_QUERY_GENERATOR = "NAIVE_BIGRAM_QUERY_GENERATOR";
  public static final String NAIVE_TRIGRAM_QUERY_GENERATOR = "NAIVE_TRIGRAM_QUERY_GENERATOR";
  public static final String BOOST_STANDARD_QUERY_GENERATOR = "BOOST_STANDARD_QUERY_GENERATOR";
  public static final String BOOST_BIGRAM_QUERY_GENERATOR = "BOOST_BIGRAM_QUERY_GENERATOR";
  public static final String BOOST_TRIGRAM_QUERY_GENERATOR = "BOOST_TRIGRAM_QUERY_GENERATOR";
  public static final String BOOST_UNIGRAM_QUERY_GENERATOR = "BOOST_UNIGRAM_QUERY_GENERATOR";
  public static final String SPAN_BIGRAM_QUERY_GENERATOR = "SPAN_BIGRAM_QUERY_GENERATOR";
  public static final String SPAN_TRIGRAM_QUERY_GENERATOR = "SPAN_TRIGRAM_QUERY_GENERATOR";

  public static final String STANDARD_ANALYZER = "STANDARD_ANALYZER";
  public static final String ENGLISH_ANALYZER = "ENGLISH_ANALYZER";
  public static final String NAIVE_FREE_QUERY_GENERATOR = "NAIVE_FREE_QUERY_GENERATOR";
}
