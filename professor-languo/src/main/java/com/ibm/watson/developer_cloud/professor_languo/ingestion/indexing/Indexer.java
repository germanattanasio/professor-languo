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

import java.io.IOException;
import java.util.Properties;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;

/**
 * This interface defines an API that creates a search index from a collection of
 * {@link StackExchangeThread} objects, which can later be queried at runtime.
 *
 */
public interface Indexer {
  /**
   * Initialize the {@link Indexer}, specifying any necessary properties
   *
   * @param properties - A collection of {@link Properties}
   * @throws IngestionException
   */
  public void initialize(Properties properties) throws IngestionException;

  /**
   * Create a new index for a given corpus of {@link StackExchangeThread}
   *
   * @param uniqueThreadDirPath - the path of the folder which contains a collection of serialized
   *        {@link StackExchangeThread} to index
   * @return An {@link IndexingStats} object, containing summary statistics about the generated
   *         index
   * @throws IngestionException
   * @throws IOException
   */
  public IndexingStats indexCorpus(String uniqueThreadDirPath) throws IngestionException;

}
