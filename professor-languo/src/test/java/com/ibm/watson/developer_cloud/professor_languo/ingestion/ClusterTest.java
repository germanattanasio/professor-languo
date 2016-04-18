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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.ibm.watson.developer_cloud.professor_languo.ingestion.Cluster;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrCreationManager;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.SolrManager;

/**
 * Test methods on the Cluster class
 */
public class ClusterTest extends SolrTestSuite {
  private ClusterTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private Cluster cluster;
  private String config_name;

  /**
   * Mock the cluster as having the configuration uploaded
   * 
   * @param config_name - the name of the configuration uploaded
   */
  private void cluster_has_config(final String config_name) {

    // if config_name is not the passed argument throw an exception
    ArgumentMatcher<String> notConfigName = new ArgumentMatcher<String>() {
      @Override public boolean matches(Object argument) {
        return !argument.toString().equals(config_name);
      }
    };

    // throw an exception if deleting a configuration that does not have the
    // configuration name
    doThrow(SolrServerException.class).when(cluster.service).deleteSolrClusterConfiguration(eq(cluster.getID()),
        argThat(notConfigName));

    // throw exception if trying to create a configuration of same name
    doThrow(SolrServerException.class).when(cluster.service)
        .uploadSolrClusterConfigurationDirectory(eq(cluster.getID()), eq(config_name), new File(anyString()));
  }

  /**
   * Create a cluster with a given id and mock manager. Mock fields that make http calls.
   * 
   * @param manager - the name of the SolrManager to use to create the cluster
   * @param id - the id of the cluster
   * @throws URISyntaxException
   */
  public void create_cluster(SolrManager manager, String id, String name, String size) throws URISyntaxException {
    when(manager.service.getSolrUrl(eq(id))).thenReturn("some_uri");
    this.cluster = new Cluster(manager, id);
    cluster.solrClient = mock(HttpSolrClient.class);
    verify(manager.service).getSolrUrl(eq(id));
  }

  /**
   * Test that an exception is thrown for a null manager
   * 
   * @throws URISyntaxException
   */
  @Test(expected = NullPointerException.class) public void cluster_from_invalid_manager() throws URISyntaxException {
    new Cluster(null, "12345");
  }

  /**
   * Test that a URI exception is thrown when a cluster is created from a bad url in the manager
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test(expected = URISyntaxException.class) public void create_cluster_from_bad_url()
      throws URISyntaxException, SolrServerException, IOException, InterruptedException {
    WHEN.creation_manager_is_from_invalid_url();
    THEN.create_cluster(mockedCreationManager, null, "name", "2");
  }

  /**
   * Test that deleting a config that doesn't exist throws a SolrServerException
   * 
   * @throws SolrServerException
   * @throws URISyntaxException
   */
  @Test(expected = SolrServerException.class) public void delete_bad_configuration()
      throws SolrServerException, URISyntaxException {
    WHEN.create_deletion_manager();
    THEN.config_name = "doesn't exit config";
    THEN.create_cluster(mockedDeletionManager, "some_id", "some_name", "some_size");
    THEN.cluster.deleteConfiguration(config_name);
  }

  /**
   * Test that after a cluster is created getID() returns the correct ID
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   */
  @Test public void test_correct_id_returned() throws URISyntaxException, SolrServerException {
    WHEN.create_deletion_manager();
    THEN.create_cluster(mockedDeletionManager, "some_id", "some_name", "some_size");
    assertEquals("some_id", cluster.getID());
  }

  /**
   * Test that attempting to create a cluster on an empty manager throw an exception
   * 
   * @throws SolrServerException
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test(expected = NullPointerException.class) public void cluster_from_empty_manager()
      throws SolrServerException, IOException, URISyntaxException {
    mockedCreationManager = mock(SolrCreationManager.class);
    Cluster cluster = new Cluster(mockedCreationManager, null);
    cluster.listCollections();
  }

  /**
   * Test that a configuration that is on the cluster can be successfully deleted
   * 
   * @throws SolrServerException
   * @throws URISyntaxException
   */
  @Test public void test_delete_config() throws SolrServerException, URISyntaxException {
    String config_name = "some_config";
    String cluster_id = "some_id";
    WHEN.create_deletion_manager();
    AND.create_cluster(mockedDeletionManager, cluster_id, "some_name", "some_size");
    AND.cluster_has_config(config_name);
    AND.manager_has_cluster(mockedDeletionManager, cluster_id);
    THEN.cluster.deleteConfiguration(config_name);;
  }

  /**
   * Test that deleting a non-existent configuration fails after a configuration has been uploaded
   * 
   * @throws SolrServerException
   * @throws URISyntaxException
   */
  @Test(expected = SolrServerException.class) public void test_delete_wrong_config()
      throws SolrServerException, URISyntaxException {
    String config_name = "some_config";
    String cluster_id = "some_id";
    WHEN.create_deletion_manager();
    AND.create_cluster(mockedDeletionManager, cluster_id, "some_name", "some_size");
    AND.cluster_has_config(config_name);
    AND.manager_has_cluster(mockedDeletionManager, cluster_id);
    THEN.cluster.deleteConfiguration("a different config");
  }

  /**
   * Test that a configuration can be successfully upploaded
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   */
  @Test public void test_upload_configuration() throws URISyntaxException, SolrServerException {
    String cluster_id = "some_id";
    GIVEN.create_creation_manager();
    WHEN.manager_has_cluster(mockedCreationManager, cluster_id);
    AND.create_cluster(mockedCreationManager, cluster_id, "some_name", "some_size");
    THEN.cluster.uploadConfiguration("test-config", "zip");
  }

  /**
   * Test that uploading a configuration under the same name twice throws an exception
   * 
   * @throws URISyntaxException
   * @throws SolrServerException
   */
  @Test(expected = SolrServerException.class) public void test_upload_configuration_twice()
      throws URISyntaxException, SolrServerException {
    String cluster_id = "some_id";
    String config_name = "some-config";
    WHEN.create_creation_manager();
    AND.manager_has_cluster(mockedCreationManager, cluster_id);
    THEN.create_cluster(mockedCreationManager, cluster_id, "some_name", "some_size");
    THEN.cluster.uploadConfiguration(config_name, "zip");
    THEN.cluster_has_config(config_name);
    THEN.cluster.uploadConfiguration(config_name, "zip");
  }
}
