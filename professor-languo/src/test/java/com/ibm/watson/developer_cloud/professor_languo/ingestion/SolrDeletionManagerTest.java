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

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrDeletionManager;

/**
 * Test methods on the SolrDeletionManager class
 */
public class SolrDeletionManagerTest extends SolrTestSuite {

  private SolrDeletionManagerTest GIVEN = this, AND = this, THEN = this;

  /**
   * Test successful deletion of existing cluster
   * 
   * @throws InterruptedException
   * @throws URISyntaxException
   * @throws SolrServerException
   */
  @Test public void test_delete_Cluster() throws InterruptedException, URISyntaxException, SolrServerException {
    String cluster_id = "some_id";
    mockedDeletionManager = mock(SolrDeletionManager.class);
    GIVEN.create_manager(mockedDeletionManager);
    AND.manager_has_cluster(mockedDeletionManager, cluster_id);
    THEN.mockedDeletionManager.deleteSolrCluster(cluster_id);
  }

  /**
   * Test URL syntax exception is thrown for invalid url
   * 
   * @throws InterruptedException
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test(expected = URISyntaxException.class) public void test_fail_delete_invalid_url_cluster()
      throws InterruptedException, URISyntaxException, SolrServerException, IOException {
    String cluster_id = "some_id";
    GIVEN.deletion_manager_is_from_invalid_url();
    THEN.mockedDeletionManager.deleteSolrCluster(cluster_id);;
  }

  /**
   * Test that deletion fails when the cluster doesn't exist
   * 
   * @throws InterruptedException
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test(expected = SolrServerException.class) public void test_fail_delete_cluster()
      throws InterruptedException, URISyntaxException, SolrServerException, IOException {
    String cluster_id = "some_id";
    GIVEN.create_deletion_manager();
    THEN.mockedDeletionManager.deleteSolrCluster(cluster_id);;
  }
}
