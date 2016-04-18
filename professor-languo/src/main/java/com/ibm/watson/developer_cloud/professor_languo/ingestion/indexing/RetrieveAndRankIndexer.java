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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.collect.Lists;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.configuration.RetrieveAndRankConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexStatFieldName;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;

/**
 * An implementation of the {@link Indexer} API that creates a Rank and Retrieve index from a
 * collection of {@link StackExchangeThread} objects.
 */
public class RetrieveAndRankIndexer implements Indexer {
  private final static Logger logger = LogManager.getLogger(RetrieveAndRankIndexer.class.getName());

  private IndexingStats indexStat = null;
  private String collection_name;

  SolrClient solrClient;

  /**
   * read Solr properties and set up connection to the solr client.
   */
  @Override public void initialize(Properties properties) throws IngestionException {

    // Read the bluemix properties
    final String username = properties.getProperty(RetrieveAndRankConstants.USERNAME);
    final String password = properties.getProperty(RetrieveAndRankConstants.PASSWORD);
    collection_name = properties.getProperty(RetrieveAndRankConstants.COLLECTION);
    final String solr_cluster_id = properties.getProperty(RetrieveAndRankConstants.SOLR_CLUSTER_ID);

    // setup client to connect to solr cluster
    final RetrieveAndRank service = new RetrieveAndRank();
    service.setUsernameAndPassword(username, password);
    final String uri = service.getSolrUrl(solr_cluster_id);
    solrClient = new WatsonSolrClient(uri, username, password);
  }

  @Override public IndexingStats indexCorpus(String uniqueThreadDirPath) throws IngestionException {
    try {
      indexStat = indexCorpus(uniqueThreadDirPath, new LuceneDocumentMapper());
    } catch (SolrServerException | RemoteSolrException | IOException e) {
      logger.error(e);
      throw new IngestionException(e);
    }
    return indexStat;
  }

  /**
   * Given a built corpus(a set of StackExchangeThreads without duplicates) and a document mapper,
   * create solr documents from the threads and upload them to the solr cluster index and record the
   * statistics during the indexing period.
   * 
   * @param uniqueThreadDirPath - the path of the folder which stores the unique threads
   * @param docMapper - document mapper which maps the StackExchange instance to the document unit
   * @return the statistics during the indexing period.
   * @throws IngestionException
   * @throws SolrServerException
   * @throws IOException
   * @throws RemoteSolrException
   */
  private IndexingStats indexCorpus(String uniqueThreadDirPath, DocumentMapper docMapper)
      throws IngestionException, SolrServerException, IOException, RemoteSolrException {

    final List<SolrInputDocument> batch = Lists.newArrayList();
    List<Integer> indexThreadIds = new ArrayList<Integer>();
    int indexDocNum = 0;
    StackExchangeThread thread = null;

    long startTime = System.currentTimeMillis();

    // restore the unique StackExchangeThreads from the .ser Files and index
    // them
    for (File serFile : new File(uniqueThreadDirPath).listFiles()) {
      thread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(serFile.getPath());

      final SolrInputDocument solrDoc = new SolrInputDocument();

      // Copy data over from thread to the SolrInputDocument
      final Document threadDoc = docMapper.createDocument(thread);
      for (IndexableField field : threadDoc.getFields()) {
        BytesRef bin = threadDoc.getBinaryValue(field.name());
        if (bin != null) {
          // Add field value as bytes if field is binary
          solrDoc.addField(field.name(), bin.bytes);
        } else {
          String value = threadDoc.get(field.name());
          if (field.name().equals(IndexDocumentFieldName.THREAD_TITLE.toString())) {
            // boost the title
            final float boostValue = 2.0f;
            solrDoc.addField(field.name(), value, boostValue);
          } else {
            solrDoc.addField(field.name(), value);
          }
        }
      }

      batch.add(solrDoc);
      indexThreadIds.add(thread.getId());

      indexDocNum++;
      // batch things to make it more efficient to index
      if (batch.size() >= 100) {
        logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.INDEXING_AMOUNT"), (indexDocNum - 100), //$NON-NLS-1$
            collection_name)); // );
        final UpdateResponse addResponse = solrClient.add(collection_name, batch);
        logger.debug(addResponse);
        batch.clear();
      }
    }

    // Include any left documents in the batch
    if (!batch.isEmpty()) {
      final UpdateResponse addResponse = solrClient.add(collection_name, batch);
      logger.debug(addResponse);
    }

    logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.INDEXING_COMMITTING"), indexDocNum)); //$NON-NLS-1$

    // Commit the documents to the index so that it will be available for
    // searching.
    final UpdateResponse commitResponse = solrClient.commit(collection_name);
    logger.debug(commitResponse);
    logger.info(Messages.getString("RetrieveAndRank.INDEXING_COMMITTED")); //$NON-NLS-1$

    long endTime = System.currentTimeMillis();

    // create the indexing stats
    return createIndexingStats(indexDocNum, indexThreadIds, endTime - startTime);
  }

  /**
   * Creates an IndexingStats object that contains the statistics of the indexing process as key
   * value like pairs
   * 
   * @param indexDocNum - the number of documents indexed
   * @param indexThreadIds - a list of the IDs for each indexed doc
   * @param period - the duration of the indexing in milliseconds
   * @return Indexing Stats - an IndexingStats object that contains the stats of the indexing
   */
  private IndexingStats createIndexingStats(int indexDocNum, List<Integer> indexThreadIds, long period) {

    IndexingStats indexingStats = new IndexingStats();

    indexingStats.addStatistic(IndexStatFieldName.INDEX_DOC_NUM.toString(), indexDocNum);
    indexingStats.addStatistic(IndexStatFieldName.INDEX_THREAD_IDS.toString(), indexThreadIds);
    indexingStats.addStatistic(IndexStatFieldName.INDEX_TIME.toString(), period);

    return indexingStats;
  }
}
