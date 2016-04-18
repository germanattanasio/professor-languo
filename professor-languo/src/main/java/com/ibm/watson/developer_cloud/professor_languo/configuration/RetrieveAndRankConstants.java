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

package com.ibm.watson.developer_cloud.professor_languo.configuration;

public final class RetrieveAndRankConstants {

  /**
   * Constants used to determine whether to use the online solr cluster or local lucene solution
   */
  public final static String SOLR = "SOLR";
  public final static String LUCENE = "LUCENE";
  public final static String PROVIDER = "PROVIDER";

  /**
   * URL constants and defaults for the indexer and searcher
   */
  public final static String CLUSTER_URL = "/solr_clusters/";
  public final static String COLLECTION_URL = "/solr/";
  public final static String RANKERS_URL = "/rankers/";
  public final static String RERANK_URL = "/rank";
  public final static String RNR_ENDPOINT_VERSION = "/v1";
  public final static String DEFUALT_CANDIDATE_ANSWER_NUM = "20";
  public final static String ANSWER_DATA = "answer_data";
  public final static String ANSWER_ID_HEADER = "answer_id";
  public final static String CONFIDENCE_HEADER = "confidence";
  public final static String QUESTION_ID_HEADER = "question_id";
  /**
   * Constants used by the solr Manager
   */
  public final static String SOLR_CLUSTER_PATH = "/solr_clusters/";

  /**
   * Constants to denote the fields of a properties object passed to the Indexer and
   * WatsonServiceSearch to provide details of the Rank and Retrieve url and credentials and the
   * default request handler
   */
  public final static String USERNAME = "USERNAME";
  public final static String PASSWORD = "PASSWORD";
  public final static String RNR_ENDPOINT = "RNR_ENDPOINT";
  public final static String COLLECTION = "COLLECTION";
  public final static String CLUSTER_SIZE = "CLUSTER_SIZE";
  public final static String SOLR_CLUSTER_ID = "SOLR_CLUSTER_ID";
  public final static String RANKER_ID = "RANKER_ID";
  public final static String CSV_FILE = "CSV_FILE";
  public final static String REQUEST_HANDLER = "REQUEST_HANDLER";
  public static final String RANKER_NAME = "RANKER_NAME";
  public static final String GROUND_TRUTH = "GROUND_TRUTH";
  public static final String ROWS = "ROWS";
  public static final String CANDIDATE_ANSWER_NUM = "CANDIDATE_ANSWER_NUM";
  public static final String TRAINING_DATA_PATH = "TRAINING_DATA_PATH";
  public static final String QUERY_RETRY_LIMIT = "QUERY_RETRY_LIMIT";

  /**
   * Constants used to read the user environment variables
   */
  public final static String VCAP_SERVICES = "VCAP_SERVICES";
  public final static String RNR_SERVICE_NAME = "retrieve_and_rank";
  public final static String CREDENTIALS_FIELD = "credentials";
  public final static String URL_FIELD = "url";
  public static final String CONFIG_PATH = "CONFIG_PATH";
  public static final String CLUSTER_NAME = "CLUSTER_NAME";
  public static final String CONFIG_NAME = "CONFIG_NAME";
}
