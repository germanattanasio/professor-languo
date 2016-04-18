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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.Indexer;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.LuceneIndexer;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.RetrieveAndRankIndexer;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.LuceneSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.Searcher;

/**
 * Factory to create and initialize a Solr or local Lucene indexer or searcher based on the
 * properties given
 */
public class IndexerAndSearcherFactory {

  public static final Logger logger = LogManager.getLogger(IndexerAndSearcherFactory.class.getName());;

  /**
   * get the indexer. This function picks the indexer type based on the input properties
   * 
   * @param properties - the properties for initialization
   * @return - the lucene or RnR indexer
   * @throws IngestionException
   */
  public static Indexer getIndexer(Properties properties) throws IngestionException {

    Indexer indexer;
    String provider = properties.getProperty(RetrieveAndRankConstants.PROVIDER);

    switch (provider) {
      case RetrieveAndRankConstants.LUCENE:
        indexer = new LuceneIndexer();
        break;
      case RetrieveAndRankConstants.SOLR:
        indexer = new RetrieveAndRankIndexer();
        break;
      default:
        String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.PROVIDER_NOT_FOUND"), provider); //$NON-NLS-1$
        throw new IllegalArgumentException(errorMsg);
    }
    try {
      loadStaticBluemixProperties(properties);
      indexer.initialize(properties);
    } catch (IllegalArgumentException | IOException e) {
      throw new IngestionException(e);
    }

    return indexer;
  }

  /**
   * get the searcher to perform queries. This function picks the searcher type based on the input
   * properties
   * 
   * @param properties - the properties for initialization
   * @return - the lucene or RnR Watson service searcher
   * @throws SearchException
   */
  public static Searcher getSearcher(Properties properties) throws SearchException {

    Searcher searcher;
    String provider = properties.getProperty(RetrieveAndRankConstants.PROVIDER);

    switch (provider) {
      case RetrieveAndRankConstants.LUCENE:
        searcher = new LuceneSearcher();
        break;
      case RetrieveAndRankConstants.SOLR:
        searcher = new RetrieveAndRankSearcher();
        break;
      default:
        String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.PROVIDER_NOT_FOUND"), provider); //$NON-NLS-1$
        throw new IllegalArgumentException(errorMsg);
    }
    try {
      loadStaticBluemixProperties(properties);
      searcher.initialize(properties);
    } catch (IllegalArgumentException | IOException e) {
      throw new SearchException(e);
    }

    return searcher;
  }

  /**
   * load bluemix properties from the server.env file to the properties object
   * 
   * @param properties
   * @return
   * @throws IOException, IllegalArgumentException
   */
  public static Properties loadStaticBluemixProperties(Properties properties)
      throws IOException, IllegalArgumentException, JsonParseException {

    // Get read the vcap services from environment
    String envServices = System.getenv(RetrieveAndRankConstants.VCAP_SERVICES);

    JsonObject services;
    if (envServices != null) {
      services = new JsonParser().parse(envServices).getAsJsonObject();

      checkStringsNotEmptyOrNull(properties, RetrieveAndRankConstants.COLLECTION, RetrieveAndRankConstants.CLUSTER_NAME,
          RetrieveAndRankConstants.CONFIG_PATH, RetrieveAndRankConstants.CONFIG_NAME,
          RetrieveAndRankConstants.RANKER_NAME, RetrieveAndRankConstants.TRAINING_DATA_PATH);

      checkStringsNotNull(properties, RetrieveAndRankConstants.SOLR_CLUSTER_ID, RetrieveAndRankConstants.CLUSTER_SIZE,
          RetrieveAndRankConstants.RANKER_ID);

    } else {
      String message = Messages.getString("RetrieveAndRank.GET_VCAP_SERVICE_IS_NULL");
      logger.debug(message);

      // Load from local env file
      Properties envProperties = new Properties();
      envProperties.load(IndexerAndSearcherFactory.class.getResourceAsStream("/server.env"));
      envServices = envProperties.getProperty(RetrieveAndRankConstants.VCAP_SERVICES);
      services = new JsonParser().parse(envServices).getAsJsonObject();

      checkStringsNotEmptyOrNull(envProperties, properties, RetrieveAndRankConstants.COLLECTION,
          RetrieveAndRankConstants.CLUSTER_NAME, RetrieveAndRankConstants.CONFIG_PATH,
          RetrieveAndRankConstants.CONFIG_NAME, RetrieveAndRankConstants.RANKER_NAME,
          RetrieveAndRankConstants.TRAINING_DATA_PATH);

      checkStringsNotNull(envProperties, properties, RetrieveAndRankConstants.SOLR_CLUSTER_ID,
          RetrieveAndRankConstants.CLUSTER_SIZE, RetrieveAndRankConstants.RANKER_ID);
    }

    loadUsernamePassword(properties, services);
    return properties;
  }

  /**
   * Checks that strings are not empty or null when loading them from the System environment into
   * the destination properties object
   * 
   * @param keys - the variable names
   * @destProperties - the destination properties
   * @throws IllegalArgumentException if any of the variables are empty or null
   */
  private static void checkStringsNotEmptyOrNull(Properties destProperties, String... keys)
      throws IllegalArgumentException {
    for (String key : keys) {
      String value = System.getenv(key);
      checkValueNotEmptyOrNull(key, value);
      destProperties.setProperty(key, value);
    }
  }

  /**
   * Checks that strings are not null when reading from the System environment variables and then
   * load the values to the destination properties file
   * 
   * @param destProperties - the properties file to load into
   * @param keys - the variable names
   * @throws IllegalArgumentException if any of the variables are null
   */
  private static void checkStringsNotNull(Properties destProperties, String... keys) throws IllegalArgumentException {
    for (String key : keys) {
      String value = System.getenv(key);
      checkValueNotNull(key, value);
      destProperties.setProperty(key, value);
    }
  }

  /**
   * Checks that strings are not null or blank when loading them to the properties file from the
   * source properties file to the destination properties file
   * 
   * @param keys
   * @throws IllegalArgumentException if any of the string arguments is blank or null
   */
  private static void checkStringsNotEmptyOrNull(Properties srcProperties, Properties destProperties, String... keys)
      throws IllegalArgumentException {
    for (String key : keys) {
      String value = srcProperties.getProperty(key);
      checkValueNotEmptyOrNull(key, value);
      destProperties.setProperty(key, value);
    }
  }

  /**
   * Checks that strings are not null when loading them to the properties file from the source
   * properties file to the destination properties file
   * 
   * @param keys
   * @throws IllegalArgumentException if any of the string are null
   */
  private static void checkStringsNotNull(Properties srcProperties, Properties destProperties, String... keys)
      throws IllegalArgumentException {
    for (String key : keys) {
      String value = srcProperties.getProperty(key);
      checkValueNotNull(key, value);
      destProperties.setProperty(key, value);
    }
  }

  /**
   * Check that a variables value is not empty or null
   * 
   * @param key - the name of the variable
   * @param value - the value of the variable
   * @throws IllegalArgumentException - thrown if the value is null or empty
   */
  private static void checkValueNotEmptyOrNull(String key, String value) throws IllegalArgumentException {
    checkValueNotNull(key, value);
    if (StringUtils.isEmpty(value)) {
      String message = MessageFormat.format(Messages.getString("RetrieveAndRank.EMPTY_CREDENTIAL"), key);
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Check that a variables value is null
   * 
   * @param key - the name of the variable
   * @param value - the value of the variable
   * @throws IllegalArgumentException - thrown if the value is null
   */
  private static void checkValueNotNull(String key, String value) throws IllegalArgumentException {
    if (value == null) {
      String message = MessageFormat.format(Messages.getString("RetrieveAndRank.NULL_CREDENTIAL"), key);
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * load the username, password and rnr url to the properties object after parsing them from the
   * VCAP json object
   * 
   * @param properties the Properties object to load the credentials into as strings
   * @param services a json object of the VCAP_SERVICES
   * @throws SearchException
   */
  private static void loadUsernamePassword(Properties properties, JsonObject services) throws IllegalArgumentException {
    final JsonArray arr = services.get(RetrieveAndRankConstants.RNR_SERVICE_NAME).getAsJsonArray();
    String username = null;
    String password = null;
    String rnrUrl = null;
    if (arr != null && arr.size() > 0) {
      // use the first RnR credentials found
      final JsonObject credentials =
          arr.get(0).getAsJsonObject().get(RetrieveAndRankConstants.CREDENTIALS_FIELD).getAsJsonObject();

      rnrUrl = credentials.get(RetrieveAndRankConstants.URL_FIELD).getAsString();

      if (credentials.get(RetrieveAndRankConstants.USERNAME.toLowerCase()) != null
          && !credentials.get(RetrieveAndRankConstants.USERNAME.toLowerCase()).isJsonNull()) {
        username = credentials.get(RetrieveAndRankConstants.USERNAME.toLowerCase()).getAsString();
      }

      if (credentials.get(RetrieveAndRankConstants.PASSWORD.toLowerCase()) != null
          && !credentials.get(RetrieveAndRankConstants.PASSWORD.toLowerCase()).isJsonNull()) {
        password = credentials.get(RetrieveAndRankConstants.PASSWORD.toLowerCase()).getAsString();
      }
    }
    // Load strings and check them
    properties.setProperty(RetrieveAndRankConstants.USERNAME, username.trim());
    properties.setProperty(RetrieveAndRankConstants.PASSWORD, password.trim());
    properties.setProperty(RetrieveAndRankConstants.RNR_ENDPOINT, rnrUrl.trim());

    for (String key : new String[] {RetrieveAndRankConstants.USERNAME, RetrieveAndRankConstants.PASSWORD,
        RetrieveAndRankConstants.RNR_ENDPOINT}) {
      checkValueNotEmptyOrNull(key, properties.getProperty(key));
    }
  }
}
