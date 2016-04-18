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

import org.apache.solr.client.solrj.SolrServerException;

/**
 * This class represents a Collection on the cluster server
 */
class Collection {
  private String name;
  private Cluster cluster;

  /**
   * Create a new collection object
   * 
   * @param cluster - the collection containing the cluster
   * @param name - the name of the collection
   */
  Collection(Cluster cluster, String name) {
    this.cluster = cluster;
    this.name = name;
  }

  /**
   * Deletes the collection from the server
   * 
   * @throws SolrServerException
   * @throws IOException
   */
  public void delete() throws SolrServerException, IOException {
    this.cluster.deleteCollection(this.name);
  }

  /**
   * Get the name of this collection
   * 
   * @return
   */
  public String getName() {
    return name;
  }

}
