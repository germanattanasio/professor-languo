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
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster.Status;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusterOptions;

/**
 * A CreationManger to support the API calls to rank and retrieve service for creating and managing
 * clusters
 */
public class SolrCreationManager extends SolrManager {
  final static Logger log = LogManager.getLogger(SolrCreationManager.class);

  /**
   * Creates a CreationManager object used to perform API calls on a remote Solr bluemix instance
   * for creation and management of clusters,collections and configurations
   * 
   * @param username - username
   * @param password - password
   * @param rnr_endpoint - the endpoint url for the rank and retrieve service
   * @throws URISyntaxException
   */
  public SolrCreationManager(String username, String password, String rnr_endpoint) throws URISyntaxException {
    super(username, password, rnr_endpoint);
  }

  /**
   * Creates a new cluster
   * 
   * @param name the name of the cluster
   * @param size the size of the cluster. Set this to empty string for a free size cluster. sizes
   *        range from 1 to 7.
   * @return the ID of the solr cluster created
   * @throws InterruptedException
   * @throws URISyntaxException
   */
  public Cluster createCluster(String name, Integer size) throws InterruptedException, URISyntaxException {

    SolrClusterOptions options = new SolrClusterOptions(name, size);
    SolrCluster cluster = service.createSolrCluster(options);

    // poll until status is ready
    log.info(Messages.getString("RetrieveAndRank.CLUSTER_CREATING")); //$NON-NLS-1$
    while (cluster.getStatus() == Status.NOT_AVAILABLE) {
      log.info(Messages.getString("RetrieveAndRank.CLUSTER_WAITING")); //$NON-NLS-1$
      cluster = service.getSolrCluster(cluster.getId());
      Thread.sleep(10000);
    }

    // cluster is now ready
    log.info("SOLR_CLUSTER_ID = " + cluster.getId());
    log.info(Messages.getString("RetrieveAndRank.CLUSTER_READY")); //$NON-NLS-1$

    return new Cluster(this, cluster.getId());
  }

  /**
   * main method
   * 
   * @param args
   * @throws SolrServerException
   * @throws InterruptedException
   * @throws IOException
   * @throws URISyntaxException
   * @throws SearchException
   */
  public static void main(String[] args)
      throws SolrServerException, InterruptedException, IOException, URISyntaxException, SearchException {

    // Load properties from the server.env
    Properties properties = new Properties();
    IndexerAndSearcherFactory.loadStaticBluemixProperties(properties);

    // credentials
    final String username = properties.getProperty(RetrieveAndRankConstants.USERNAME);
    final String password = properties.getProperty(RetrieveAndRankConstants.PASSWORD);
    final String rnr_enpoint = properties.getProperty(RetrieveAndRankConstants.RNR_ENDPOINT);

    // collection and configuration names
    final String collection_name = properties.getProperty(RetrieveAndRankConstants.COLLECTION);
    final String configuration_name = properties.getProperty(RetrieveAndRankConstants.CONFIG_NAME);

    // cluster name and size
    final String cluster_name = properties.getProperty(RetrieveAndRankConstants.CLUSTER_NAME);
    final Integer cluster_size = Integer.parseInt(properties.getProperty(RetrieveAndRankConstants.CLUSTER_SIZE));

    // path to configuration zip file to be uploaded
    final String config_path = properties.getProperty(RetrieveAndRankConstants.CONFIG_PATH);

    SolrCreationManager solrCreationManger = new SolrCreationManager(username, password, rnr_enpoint);

    // create a cluster and get its id
    String cluster_id = solrCreationManger.createCluster(cluster_name, cluster_size).getID();

    // print cluster ids
    System.out.println(solrCreationManger.listClusterIDs());

    // choose a cluster
    Cluster cluster = solrCreationManger.getCluster(cluster_id);

    // show cluster status
    System.out.println(cluster.getStatus());

    // wait for status to be available.
    cluster.waitTillReady(5000);

    // upload a configuration
    cluster.uploadConfiguration(configuration_name, config_path);

    // wait for status to be available
    cluster.waitTillReady(5000);

    // create a collection
    cluster.createCollection(collection_name, configuration_name);

    // wait for status to be available
    cluster.waitTillReady(5000);

    // show cluster collections
    System.out.println(cluster.listCollections());
  }
}
