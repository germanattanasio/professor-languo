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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;

public class SolrDeletionManager extends SolrManager {
  final static Logger log = LogManager.getLogger(SolrDeletionManager.class);

  /**
   * Creates a Deletion object used to perform API calls on a remote Solr bluemix instance for
   * deletion of clusters, collections and configurations
   * 
   * @param username - username
   * @param password - password
   * @param rnr_enpoint - the endpoint url for the rank and retrieve service
   * @throws URISyntaxException
   */
  public SolrDeletionManager(String username, String password, String rnr_endpoint) throws URISyntaxException {
    super(username, password, rnr_endpoint);
  }

  /**
   * Delete the cluster
   * 
   * @throws SolrServerException
   */
  public void deleteSolrCluster(String cluster_id) throws SolrServerException {
    log.info(MessageFormat.format(Messages.getString("RetrieveAndRank.CLUSTER_DELETING"), cluster_id)); //$NON-NLS-1$ )
    List<SolrCluster> clusters = service.getSolrClusters().getSolrClusters();

    // check that the cluster exists on the server and delete
    for (SolrCluster cluster : clusters) {
      if (cluster.getId().equals(cluster_id)) {
        service.deleteSolrCluster(cluster_id);
        log.info(Messages.getString("RetrieveAndRank.CLUSTER_DELETED")); //$NON-NLS-1$
        return;
      }
    }

    // throw exception if the cluster is not found
    throw new SolrServerException(
        MessageFormat.format(Messages.getString("RetrieveAndRank.CLUSTER_UNKNOWN"), cluster_id)); //$NON-NLS-1$ )
  }

  /**
   * main method
   * 
   * @param args
   * @throws SolrServerException
   * @throws InterruptedException
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void main(String[] args)
      throws SolrServerException, InterruptedException, IOException, URISyntaxException {

    // Load properties from the server.env
    Properties properties = new Properties();
    IndexerAndSearcherFactory.loadStaticBluemixProperties(properties);

    final String username = properties.getProperty(RetrieveAndRankConstants.USERNAME);
    final String password = properties.getProperty(RetrieveAndRankConstants.PASSWORD);
    final String rnr_enpoint = properties.getProperty(RetrieveAndRankConstants.RNR_ENDPOINT);

    // collection and configuration names
    final String collection_name = properties.getProperty(RetrieveAndRankConstants.COLLECTION);
    final String configuration_name = properties.getProperty(RetrieveAndRankConstants.CONFIG_NAME);

    // cluster id
    String cluster_id = properties.getProperty(RetrieveAndRankConstants.SOLR_CLUSTER_ID);

    SolrDeletionManager solrDeletionManager = new SolrDeletionManager(username, password, rnr_enpoint);

    // print cluster ids
    System.out.println(solrDeletionManager.listClusterIDs());

    // choose cluster
    Cluster cluster = solrDeletionManager.getCluster(cluster_id);

    // get collection
    Collection collection = cluster.getCollection(collection_name);

    // delete configuration
    cluster.deleteConfiguration(configuration_name);

    // delete collection
    cluster.deleteCollection(collection.getName());

    // clean a cluster (delete all its collections and configurations)
    cluster.clean();

    // delete cluster
    solrDeletionManager.deleteSolrCluster(cluster_id);
  }

}
