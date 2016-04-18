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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;

/**
 * A Manager to support the API calls to rank and retrieve service for creating and managing
 * clusters
 */
public abstract class SolrManager {
  final static Logger log = LogManager.getLogger(SolrManager.class.getName());

  String username;
  String password;
  String retrieve_and_rank_endpoint;

  RetrieveAndRank service;

  /**
   * Creates a SolrManager object used to perform API calls on a remote Solr bluemix instance
   * 
   * @param username - username
   * @param password - password
   * @param rnr_enpoint - the endpoint url for the rank and retrieve service
   * @throws URISyntaxException
   */
  public SolrManager(String username, String password, String rnr_endpoint) throws URISyntaxException {
    this.password = password;
    this.username = username;
    this.retrieve_and_rank_endpoint = rnr_endpoint + RetrieveAndRankConstants.RNR_ENDPOINT_VERSION;

    service = new RetrieveAndRank();
    service.setUsernameAndPassword(username, password);

  }

  /**
   * Get a cluster by ID
   * 
   * @param cluster_id - the Id of the cluster
   * @return
   * @throws IOException
   * @throws SolrServerException
   * @throws URISyntaxException
   */
  public Cluster getCluster(String cluster_id) throws SolrServerException, IOException, URISyntaxException {
    if (listClusterIDs().contains(cluster_id)) {
      return new Cluster(this, cluster_id);
    } else {
      throw new SolrServerException(
          MessageFormat.format(Messages.getString("RetrieveAndRank.CLUSTER_UNKNOWN"), cluster_id)); //$NON-NLS-1$ )
    }
  }

  /**
   * lists all the ids of clusters on the server
   * 
   * @return List<String> - a list of all ids
   * @throws SolrServerException
   * @throws IOException
   */
  public List<String> listClusterIDs() throws SolrServerException, IOException {
    final List<String> clusters = new ArrayList<String>();

    for (SolrCluster response : service.getSolrClusters().getSolrClusters()) {
      clusters.add(response.getId());
    }

    return clusters;
  }
}
