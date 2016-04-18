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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;
import org.mockito.ArgumentMatcher;

import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrCreationManager;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrDeletionManager;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrManager;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster.Status;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusterList;

/**
 * An abstract class use to provide common methods to test classes
 */
public abstract class SolrTestSuite {

  SolrCreationManager mockedCreationManager;
  SolrDeletionManager mockedDeletionManager;

  /**
   * Set up a mocked creation manager
   */
  public void create_creation_manager() {
    mockedCreationManager = mock(SolrCreationManager.class);
    create_manager(mockedCreationManager);
  }

  /**
   * Set up a deletion manager and mock thrown exceptions by default when deletions are attempted on
   * the new manager as it has no clusters yet
   * 
   * @throws SolrServerException
   */
  public void create_deletion_manager() throws SolrServerException {
    mockedDeletionManager = mock(SolrDeletionManager.class);
    doThrow(SolrServerException.class).when(mockedDeletionManager).deleteSolrCluster(anyString());
    create_manager(mockedDeletionManager);
  }

  /**
   * Mock creation of a creation manager from an invalid url
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   * @throws InterruptedException
   */
  @SuppressWarnings("unchecked") public void creation_manager_is_from_invalid_url()
      throws URISyntaxException, SolrServerException, IOException, InterruptedException {
    create_creation_manager();
    mockedCreationManager = (SolrCreationManager) create_manager_from_invalid_url(mockedCreationManager);
    when(mockedCreationManager.createCluster(anyString(), anyInt())).thenThrow(URISyntaxException.class);
  }

  /**
   * create manger from invalid url
   * 
   * @param manager - deletion or creation manager to create
   * @return manager - the created manager
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   */
  @SuppressWarnings("unchecked") public SolrManager create_manager_from_invalid_url(SolrManager manager)
      throws URISyntaxException, SolrServerException, IOException {
    manager.retrieve_and_rank_endpoint = "bad url";

    // For an invalid manager url, mock exceptions for creating and listing
    // clusters
    when(manager.getCluster(anyString())).thenThrow(URISyntaxException.class);
    when(manager.listClusterIDs()).thenThrow(SolrServerException.class);

    return manager;
  }

  /**
   * create deletion manager from invalid url
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   * @throws InterruptedException
   */
  public void deletion_manager_is_from_invalid_url()
      throws URISyntaxException, SolrServerException, IOException, InterruptedException {
    mockedDeletionManager = mock(SolrDeletionManager.class);
    mockedDeletionManager = (SolrDeletionManager) create_manager_from_invalid_url(mockedDeletionManager);

    // For an invalid manager, mock exceptions for deletion requests
    doThrow(URISyntaxException.class).when(mockedDeletionManager).deleteSolrCluster(anyString());
  }

  /**
   * create a manager with mocked fields by populating a given manager
   * 
   * @param manager - creation or deletion manager to populate
   */
  public void create_manager(SolrManager manager) {
    manager.username = "username";
    manager.password = "password";
    manager.retrieve_and_rank_endpoint = "endpoint";
    manager.service = mock(RetrieveAndRank.class);

    // mock listing of cluster gives an empty array
    when(manager.service.getSolrClusters()).thenReturn(new SolrClusterList(new ArrayList<SolrCluster>()));
  }

  /**
   * Mock a manager as containing a cluster. Call to list clusters on this manager will list the
   * given cluster
   * 
   * @param manager - creation or deletion manager to contain the cluster
   * @param cluster_id - the id of the cluster to be contained on the manager
   * @throws SolrServerException
   */
  public void manager_has_cluster(SolrManager manager, final String cluster_id) throws SolrServerException {
    // mock listing of cluster shows the cluster
    ArrayList<SolrCluster> responses = new ArrayList<SolrCluster>();

    String cluster_name = "";
    String cluster_size = "";

    responses.add(new SolrCluster(cluster_id, cluster_name, cluster_size, Status.READY));
    when(manager.service.getSolrClusters()).thenReturn(new SolrClusterList(responses));

    when(manager.service.getSolrUrl(eq(cluster_id))).thenReturn("some_uri");

    when(manager.service.getSolrCluster(eq(cluster_id)))
        .thenReturn(new SolrCluster(cluster_id, cluster_name, cluster_size, Status.READY));

    // If a deletion manager is defined. Throw exceptions on if attempts are
    // made to delete clusters
    // other than
    // the mocked cluster.
    if (mockedDeletionManager != null) {
      class IsNotCluster extends ArgumentMatcher<String> {
        public boolean matches(Object id) {
          return !id.toString().equals(cluster_id);
        }
      }
      doThrow(SolrServerException.class).when(mockedDeletionManager).deleteSolrCluster(argThat(new IsNotCluster()));
    }
  }
}
