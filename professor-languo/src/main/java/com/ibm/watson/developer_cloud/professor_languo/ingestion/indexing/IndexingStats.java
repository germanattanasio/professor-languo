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

package com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@link Map} of name-value pairs, providing summary statistics about the indexing of a given
 * corpus, e.g., # of documents created, list of document fields, etc.
 *
 */
public class IndexingStats extends HashMap<String, Object> {

  private static final long serialVersionUID = -7744913778617767548L;

  /**
   * Create a new {@link IndexingStats} object with an empty mapping and default capacity
   */
  public IndexingStats() {
    super();
  }

  /**
   * Add a new summary statistic to the map
   *
   * @param name - The name of the statistic, e.g., "numDocs"
   * @param statistic - The value of the statistic, e.g., "1234"
   * @return The previous value of the statistic, or <code>null</code> if there was no prior mapping
   *         for the given <code>name</code>
   */
  public Object addStatistic(String name, Object statistic) {
    return put(name, statistic);
  }

  /**
   * Retrieve a summary statistic from the map
   *
   * @param name - The name of the statistic to retrieve
   * @return - The value of the statistic, or <code>null</code> if this map contains no such
   *         statistic
   */
  public Object getStatistic(String name) {
    return get(name);
  }

  /**
   * @return A set of names for the statistics currently stored in the map
   */
  public Set<String> getStatistics() {
    return keySet();
  }

}
