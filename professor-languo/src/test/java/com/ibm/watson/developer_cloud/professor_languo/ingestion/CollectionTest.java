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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.ibm.watson.developer_cloud.professor_languo.ingestion.Cluster;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.Collection;

public class CollectionTest {

  private Cluster cluster;
  private Collection collection;

  private CollectionTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  /**
   * Create a mock cluster to test the collection on
   * 
   * @throws SolrServerException
   */
  public void create_cluster() throws SolrServerException {
    cluster = mock(Cluster.class);
  }

  /**
   * create a collection from the mocked cluster
   * 
   * @param name - the name of the collection
   * @throws SolrServerException
   * @throws IOException
   */
  private void create_collection_from_cluster(String name) throws SolrServerException, IOException {
    collection = new Collection(cluster, name);
  }

  /**
   * Mock the cluster as already containing a collection
   * 
   * @param name - the name of the collection the cluster should contain
   * @throws SolrServerException
   * @throws IOException
   */
  public void cluster_has_collection(final String name) throws SolrServerException, IOException {
    // if config_name is not the passed argument throw an exception

    ArgumentMatcher<String> notCollectionName = new ArgumentMatcher<String>() {
      @Override public boolean matches(Object argument) {
        return !argument.toString().equals(name);
      }
    };

    doThrow(SolrServerException.class).when(cluster).deleteCollection(argThat(notCollectionName));
  }

  /**
   * Test that a collection is successfully created on a cluster
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_collection_creation() throws SolrServerException, IOException {
    String collection_name = "collection";
    WHEN.create_cluster();
    THEN.create_collection_from_cluster(collection_name);
  }

  /**
   * Test that a collection on the cluster can be successfully deleted
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_delete_collection() throws SolrServerException, IOException {
    String collection_name = "collection";
    GIVEN.create_cluster();
    THEN.create_collection_from_cluster(collection_name);
    AND.cluster_has_collection(collection_name);
    THEN.collection.delete();
  }

  /**
   * Test that deleting a collection not on the cluster throws a SorlServerException
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @Test(expected = SolrServerException.class) public void test_fail_delete_collection()
      throws SolrServerException, IOException {
    String collection_name = "collection";
    WHEN.create_cluster();
    AND.cluster_has_collection("another_collection");
    THEN.create_collection_from_cluster(collection_name);
    THEN.collection.delete();
  }

  /**
   * Test that after a collection is created calling getName() gives the correct name
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  @Test public void test_get_name() throws SolrServerException, IOException {
    String collection_name = "collection";
    WHEN.create_cluster();
    THEN.create_collection_from_cluster(collection_name);
    assertEquals(collection_name, collection.getName());
  }

}
