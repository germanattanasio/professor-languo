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

/**
 * Constants that are used during configuration of the professor-languo application (both runtime
 * and ingestion-time), e.g., property names.
 *
 */
public class ConfigurationConstants {

  // Names of ingestion-specific properties
  public static final String INGESTION_BASE_DIR = "RES_DIR_PATH";
  public static final String INDEX_DIR = "INDEX_DIR_PATH";
  public static final String INDEX_DIR_TYPE = "INDEX_DIR_TYPE";
  public static final String INDEX_STAT_PATH = "INDEX_STAT_PATH";
  public static final String DUPLICATE_THREAD_TSV_PATH = "DUPLICATE_THREAD_TSV_PATH";
  public static final String DUPLICATE_THREAD_DIR = "DUPLICATE_THREAD_DIR";
  public static final String UNIQUE_THREAD_SER_PATH = "UNIQUE_THREAD_SER_PATH";
  public static final String CORPUS_XML_DIR = "XML_DIR_PATH";
  public static final String CORPUS_POSTS_XML_FILENAME = "postsXmlFile";
  public static final String CORPUS_USERS_XML_FILENAME = "usersXmlFile";
  public static final String CORPUS_VOTES_XML_FILENAME = "votesXmlFile";
  public static final String CORPUS_POSTLINKS_XML_FILENAME = "postLinksXmlFile";
  public static final String CORPUS_SITE_NAME = "site";
  public static final String ANALYZER = "ANALYZER";
  public static final String QUERY_GENERATORS = "QUERY_GENERATORS";
  public static final String CANDIDATE_ANSWER_NUM = "CANDIDATE_ANSWER_NUM";
  public static final String CANDIDATE_ANSWER_NUM_PER_QUERY_COMPONENT = "CANDIDATE_ANSWER_NUM_PER_QUERY_COMPONENT";
  public static final String EGA_METADATA_FEATURE_SCORERS = "EGA_METADATA_FEATURE_SCORERS";

  // Ingestion-specific property values
  public enum IndexDirTypes {
    RAM, FS
  };

  // Names of runtime pipeline-specific properties
  public static final String PIPELINE_QUESTION_ANSWERER = "PIPELINE_QUESTION_ANSWERER";
  public static final String QUESTION_SET_MANAGER_RAND_NUM_SEED = "QUESTION_SET_MANAGER_RAND_NUM_SEED";
  public static final String QUESTION_SET_MANAGER_PARTITION_FRACTIONS = "QUESTION_SET_MANAGER_PARTITION_FRACTIONS";

  // Names of evaluation-specific properties
  public static final String PIPELINE_RESULTS_TSV_FILE_PATH = "RESULTS_FILE_PATH";
  public static final String PIPELINE_GOLD_STANDARD_TSV_FILE_PATH = "PIPELINE_GOLD_STANDARD_TSV_FILE_PATH";
  public static final String PIPELINE_RESULTS_TSV_FILE_FORMAT = "RESULTS_FILE_FORMAT";

  // Evaluation-specific property values
  public enum PipelineResultsTsvFileFormats {
    DEFAULT, VERBOSE, COMPETITION
  };
}
