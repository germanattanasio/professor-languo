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

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

/**
 * Test methods of the SolrCreationManager class
 */
public class SolrCreationManagerTest extends SolrTestSuite {

  private SolrCreationManagerTest GIVEN = this, THEN = this;

  /**
   * Test that a cluster can be created by the manager
   * 
   * @throws InterruptedException
   * @throws URISyntaxException
   */
  @Test public void test_create_cluster() throws InterruptedException, URISyntaxException {
    GIVEN.create_creation_manager();
    THEN.mockedCreationManager.createCluster("name", 0);
  }

  /**
   * Test that cluster creation fails if manager has invalid url
   * 
   * @throws InterruptedException
   * @throws URISyntaxException
   * @throws SolrServerException
   * @throws IOException
   */
  @Test(expected = URISyntaxException.class) public void test_fail_create_cluster()
      throws InterruptedException, URISyntaxException, SolrServerException, IOException {
    GIVEN.creation_manager_is_from_invalid_url();
    THEN.mockedCreationManager.createCluster("name", 0);
  }
}
