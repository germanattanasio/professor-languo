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

package com.ibm.watson.developer_cloud.professor_languo.ingestion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster.Status;

/**
 * This class represents a Solr Cluster on the server
 */
class Cluster {
  private String id;
  String name;
  String size;
  RetrieveAndRank service;
  HttpSolrClient solrClient;
  final static Logger log = LogManager.getLogger(Cluster.class);

  /**
   * Create new cluster object to represent a cluster on the server
   * 
   * @param solrManager - A SolrManager with the connection details
   * @param id - the id of the cluster
   * @throws URISyntaxException
   */
  public Cluster(SolrManager solrManager, String id) throws URISyntaxException {
    this.id = id;
    URI solrUri =
        new URI(solrManager.retrieve_and_rank_endpoint + RetrieveAndRankConstants.SOLR_CLUSTER_PATH + this.id);
    log.info(solrUri);
    // 1 create the service
    service = solrManager.service;
    String uri = service.getSolrUrl(id);
    solrClient =
        new HttpSolrClient(uri, RankerCreationUtil.createHttpClient(uri, solrManager.username, solrManager.password));
  }

  /**
   * Get the status of the cluster to check
   * 
   * @return
   * @throws SolrServerException
   */
  public Status getStatus() throws SolrServerException {
    SolrCluster cluster = service.getSolrCluster(this.id);
    if (cluster != null) {
      return cluster.getStatus();
    }
    String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.UNKNOWN_CLUSTER_ID"), //$NON-NLS-1$
        id, service.getSolrClusters().getSolrClusters());
    throw new SolrServerException(errorMsg);
  }

  /**
   * Get the cluster ID
   * 
   * @return - The cluster ID string
   */
  public String getID() {
    return this.id;
  }

  /**
   * upload the configuration zip to the cluster
   * 
   * @param config_name - name to save this configuration under
   * @param path_config_zip - path to zip with scheme.xml and solrconfig.xml
   * @throws SolrServerException
   */
  public void uploadConfiguration(String config_name, String path_config_zip) throws SolrServerException {
    checkStatus();
    log.info(Messages.getString("RetrieveAndRank.CONFIG_UPLOADING")); //$NON-NLS-1$
    service.uploadSolrClusterConfigurationDirectory(id, config_name, new File(path_config_zip));
    log.info(Messages.getString("RetrieveAndRank.CONFIG_UPLOADED")); //$NON-NLS-1$
  }

  /**
   * Deletes a configuration.
   * 
   * @param config_name - the configuration to delete
   * @throws SolrServerException
   */
  public void deleteConfiguration(String config_name) throws SolrServerException {
    checkStatus();
    log.info(Messages.getString("RetrieveAndRank.CONFIG_DELETING")); //$NON-NLS-1$
    service.deleteSolrClusterConfiguration(id, config_name);
    log.info(Messages.getString("RetrieveAndRank.CONFIG_DELETED")); //$NON-NLS-1$
  }

  /**
   * Delete a collection
   * 
   * @param collection_name - collection to be deleted if it exists
   * @param config_name - configuration to be deleted if it exists
   * @throws SolrServerException
   * @throws IOException
   */
  void deleteCollection(String collection_name) throws SolrServerException, IOException {
    checkStatus();
    final CollectionAdminRequest.Delete deleteCollectionRequest = new CollectionAdminRequest.Delete();
    deleteCollectionRequest.setCollectionName(collection_name);

    // Send the deletion request and throw an exception if the response is
    // not successful
    log.info(Messages.getString("RetrieveAndRank.COLLECTION_DELETING")); //$NON-NLS-1$

    final CollectionAdminResponse response = deleteCollectionRequest.process(solrClient);
    if (!response.isSuccess()) {
      throw new IllegalStateException(
          MessageFormat.format(Messages.getString("RetrieveAndRank.COLLECTION_DELETE_FAILED"), //$NON-NLS-1$
              response.getErrorMessages().toString()));
    }
    log.info(Messages.getString("RetrieveAndRank.COLLECTION_DELETED")); //$NON-NLS-1$
  }

  /**
   * list all collection names on the cluster
   * 
   * @return List<String> - a list of the names of the collections
   * @throws SolrServerException
   * @throws IOException
   */
  @SuppressWarnings("unchecked") public List<String> listCollections() throws SolrServerException, IOException {
    checkStatus();
    final CollectionAdminRequest.List listCollectionRequest = new CollectionAdminRequest.List();
    final CollectionAdminResponse listResponse = listCollectionRequest.process(solrClient);
    final List<String> collections = (List<String>) listResponse.getResponse().get("collections");

    // check that the response is successful and return the list
    if (!listResponse.isSuccess()) {
      return collections;
    } else {
      String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.COLLECTION_LIST"), listResponse); //$NON-NLS-1$
      throw new SolrServerException(errorMsg);
    }
  }

  /**
   * Get collection object representing the collection on cluster on the server
   * 
   * @param collection_name - name of collection
   * @return Collection on this cluster
   * @throws SolrServerException
   * @throws IOException
   */
  public Collection getCollection(String collection_name) throws SolrServerException, IOException {
    checkStatus();
    List<String> collections = listCollections();
    if (collections.contains(collection_name)) {
      // return new collection object
      return new Collection(this, collection_name);
    } else {
      String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.COLLECTION_UNKNOWN"), //$NON-NLS-1$
          collection_name);
      throw new SolrServerException(errorMsg);
    }
  }

  /**
   * Deletes all configurations and collections from a cluster.
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  public void clean() throws SolrServerException, IOException {
    checkStatus();
    final CollectionAdminRequest.List listCollectionRequest = new CollectionAdminRequest.List();
    final CollectionAdminResponse listResponse = listCollectionRequest.process(solrClient);
    @SuppressWarnings("unchecked") final List<String> collections =
        (List<String>) listResponse.getResponse().get("collections");
    for (final String collection : collections) {
      deleteCollection(collection);
    }
    for (final String config : service.getSolrClusterConfigurations(id)) {
      deleteConfiguration(config);
    }
  }

  /**
   * creates a collection with the specified configuration
   * 
   * @param collection_name - the new collection's name
   * @param config_name - name of already uploaded configuration to use for this collection
   * @throws SolrServerException
   * @throws IOException
   */
  public Collection createCollection(String collection_name, String config_name)
      throws SolrServerException, IOException {
    checkStatus();
    final CollectionAdminRequest.Create createCollectionRequest = new CollectionAdminRequest.Create();
    createCollectionRequest.setCollectionName(collection_name);
    createCollectionRequest.setConfigName(config_name);

    log.info(Messages.getString("RetrieveAndRank.COLLECTION_CREATING")); //$NON-NLS-1$
    final CollectionAdminResponse response = createCollectionRequest.process(solrClient);
    if (!response.isSuccess()) {
      String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.COLLECTION_CREATE_FAILED"), //$NON-NLS-1$
          response.getErrorMessages().toString());
      throw new IllegalStateException(errorMsg);
    }
    log.info(Messages.getString("RetrieveAndRank.COLLECTION_CREATED")); //$NON-NLS-1$
    Collection collection = new Collection(this, collection_name);
    return collection;
  }

  /**
   * Check that the cluster is available
   * 
   * @throws SolrServerException
   */
  private void checkStatus() throws SolrServerException {
    if (this.getStatus().equals(Status.NOT_AVAILABLE)) {
      String errorMsg = MessageFormat.format(Messages.getString("RetrieveAndRank.CLUSTER_STATUS"), //$NON-NLS-1$
          Status.NOT_AVAILABLE);
      throw new SolrServerException(errorMsg);
    }
  }

  /**
   * Polls until the cluster is available. A longer poll interval gives more stable results.
   * 
   * @param interval - the time in milliseconds between polls
   * @throws InterruptedException
   * @throws SolrServerException
   */
  public void waitTillReady(long interval) throws InterruptedException, SolrServerException {
    Thread.sleep(interval);
    while (this.getStatus().equals(Status.NOT_AVAILABLE)) {
      Thread.sleep(interval);
    }
    Thread.sleep(interval);
  }
}
