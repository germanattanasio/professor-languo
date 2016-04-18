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

/**
 * Constants used by the rank and retrieve searcher service
 */
public class RetrieveAndRankSearcherConstants {
  /**
   * Constants used by the Rank and Retrieve searcher for making performing search queries
   */
  public static final String FCSELECT_REQUEST_HANDLER = "/fcselect";
  public static final String SELECT_REQUEST_HANDLER = "/select";
  public static final String RANKER_REQUEST_HANDLER = "/rankers";
  public final static String RANK_REQUEST_HANDLER = "/rank";
  public static final String FEATURE_VECTOR_DELIM = " ";
  public static final char CSV_DELIM = ',';
  public static final String ENCODING = "UTF-8";
  public static final int MAX_QUERY_ATTEMPTS = 3;
  public static final int MAX_CLAUSE_COUNT = 4096;
  public static final String FEATURE_HEADER = "feature";
  public static final int NUM_GENEATED_FEATURES = 39;
  public static final String GT_HEADER = "ground_truth";

  /**
   * Specify parameters for making search queries to the RnR searcher
   */
  public static final String QUESTION_PARAM = "q";
  public static final String RS_INPUT_PARAM = "returnRSInput";
  public static final String ROWS_PARAM = "rows";
  public static final String GROUND_TRUTH_PARAM = "gt";
  public static final String GENERATE_HEADER_PARAM = "generateHeader";
  public static final String DOCTYPE_PARAM = "wt";
  public static final String FIELD_LIST_PARAM = "fl";

  /**
   * Field values for making search queries to the RnR searcher
   */
  public static final String ALL_FIELDS = "*";
  public static final String FEATURE_VECTOR_FIELD = "featureVector";
  public static final String SCORE_FIELD = "score";
  public static final String ID_FIELD = "THREAD_POST_ID";

  /**
   * Form labels for training the ranker
   */
  public static final String TRAINING_DATA_LABEL = "training_data";
  public static final String TRAINING_METADATA_LABEL = "training_metadata";
  public static final String RANKER_ID = "ranker_id";
}
