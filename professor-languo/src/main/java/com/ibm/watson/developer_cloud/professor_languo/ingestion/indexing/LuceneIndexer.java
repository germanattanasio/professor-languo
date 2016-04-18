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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexStatFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.LuceneSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.SingletonAnalyzer;

/**
 * An implementation of the {@link Indexer} API that creates a Lucene index from a collection of
 * {@link StackExchangeThread} objects.
 *
 */
public class LuceneIndexer implements Indexer {
  private final static Logger logger = LogManager.getLogger(LuceneIndexer.class.getName());

  /**
   * Used by Lucene to write document into the index file
   */
  private IndexWriter indexWriter = null;

  /**
   * every {@link LuceneIndexer} has a {@link DocumentMapper} associated with it, which is used to
   * convert StackExchangeThread into a LuceneDocument
   */
  private DocumentMapper documentMapper = null;

  /**
   * The (RAM or disk) directory used to store the Lucene index files, which needs to be accessed by
   * the {@link LuceneSearcher} to perform search
   */
  private Directory indexDir = null;
  private String indexDirPath = null;

  /**
   * The path of the index statistics file which contains the summary of the indexing procedure
   */
  private String indexStatPath = null;
  /**
   * The summary of the statistics during the indexing procedure
   */
  private IndexingStats indexStat = null;

  @Override public void initialize(Properties properties) throws IngestionException {
    String resDirPath = properties.getProperty(ConfigurationConstants.INGESTION_BASE_DIR) + File.separator;
    indexStatPath = resDirPath + properties.getProperty(ConfigurationConstants.INDEX_STAT_PATH);

    if (properties.getProperty(ConfigurationConstants.INDEX_DIR_TYPE).toUpperCase()
        .equals(ConfigurationConstants.IndexDirTypes.RAM.toString()))
      indexDir = new RAMDirectory();
    else if (properties.getProperty(ConfigurationConstants.INDEX_DIR_TYPE).toUpperCase()
        .equals(ConfigurationConstants.IndexDirTypes.FS.toString())) {
      try {
        indexDirPath = resDirPath + properties.getProperty(ConfigurationConstants.INDEX_DIR);
        indexDir = FSDirectory.open(new File(indexDirPath).toPath());
        // clear the previous index files in the folder before a new
        // indexing process begins
        clearIndexDirectory(indexDir);
      } catch (IngestionException | IOException e) {
        throw new IngestionException(e);
      }
    } else {
      throw new IngestionException("Unrecognized " + ConfigurationConstants.INDEX_DIR_TYPE + ": "
          + properties.getProperty(ConfigurationConstants.INDEX_DIR_TYPE));
    }
  }

  @Override public IndexingStats indexCorpus(String uniqueThreadDirPath) throws IngestionException {
    IndexWriter writer = getIndexWriter();
    DocumentMapper docMapper = getDocumentMapper();
    indexStat = indexCorpus(uniqueThreadDirPath, writer, docMapper);
    saveIndexStatToDisk();
    return indexStat;
  }

  /**
   * Given a built corpus(a set of StackExchangeThreads without duplicates), an index writer and a
   * document mapper, write the indexing file with documents and record the statistics during the
   * indexing period.
   * 
   * @param uniqueThreadDirPath - the path of the folder which stores the unique threads
   * @param writer - an index writer which can write document unit to the index file
   * @param docMapper - document mapper which maps the StackExchange instance to the document unit
   * @return the statistics during the indexing period.
   * @throws IngestionException
   */
  private IndexingStats indexCorpus(String uniqueThreadDirPath, IndexWriter writer, DocumentMapper docMapper)
      throws IngestionException {
    List<Integer> indexThreadIds = new ArrayList<Integer>();
    long startTime, endTime;
    int indexDocNum;
    StackExchangeThread thread = null;
    File[] serFiles = new File(uniqueThreadDirPath).listFiles();

    try {
      startTime = System.currentTimeMillis();
      // restore the uniqe StackExchangeThreads from the .ser Files and
      // index them
      for (File serFile : serFiles) {
        thread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(serFile.getPath());
        Document doc = docMapper.createDocument(thread);
        writer.addDocument(doc);
        indexThreadIds.add(thread.getId());
      }

      endTime = System.currentTimeMillis();
      indexDocNum = writer.numDocs();
      closeIndexWriter();
    } catch (IOException e) {
      throw new IngestionException(e);
    }

    return createIndexingStats(indexDocNum, indexThreadIds, endTime - startTime);
  }

  /**
   * Create a indexingStat instance to record the the statistics during the indexing period.
   * 
   * @param indexDocNum - the number of documents indexed
   * @param indexThreadIds - the post ids of the threads indexed
   * @param period - the time consumed in the indexing peroid
   * @return
   */
  private IndexingStats createIndexingStats(int indexDocNum, List<Integer> indexThreadIds, long period) {
    IndexingStats indexingStats = new IndexingStats();
    indexingStats.addStatistic(IndexStatFieldName.INDEX_DOC_NUM.toString(), indexDocNum);
    indexingStats.addStatistic(IndexStatFieldName.INDEX_THREAD_IDS.toString(), indexThreadIds);
    indexingStats.addStatistic(IndexStatFieldName.INDEX_TIME.toString(), period);
    return indexingStats;
  }

  /**
   * Set the mapper of the indexer
   * 
   * @param mapper - the mapper associated with the indexer
   */
  public void setDocumentMapper(DocumentMapper mapper) {
    documentMapper = mapper;
  }

  /**
   * Get the mapper of the indexer
   * 
   * @return the mapper associated with the indexer
   */
  public DocumentMapper getDocumentMapper() {
    if (documentMapper == null) {
      return new LuceneDocumentMapper();
    }
    return documentMapper;
  }

  /**
   * Get the index writer in order to perform adding the documents to the index file. Initialize the
   * index writer if it hasn't been created.
   * 
   * @return the index writer which can add the documents to the index
   * @throws IngestionException
   */
  private IndexWriter getIndexWriter() throws IngestionException {
    if (indexWriter == null) {
      try {
        IndexWriterConfig config = new IndexWriterConfig(SingletonAnalyzer.getAnalyzer());
        indexWriter = new IndexWriter(indexDir, config);
      } catch (IOException e) {
        logger.fatal(Messages.getString("RetrieveAndRank.DIR_OPEN_FAIL")); //$NON-NLS-1$
        throw new IngestionException(e);
      }
    }
    return indexWriter;
  }

  /**
   * Close the {@link IndexWriter} after all the documents have been added to the index. Flush the
   * index to make it take effect!
   * 
   * @throws IOException
   */
  public void closeIndexWriter() throws IOException {
    if (indexWriter != null) {
      indexWriter.close();
    }
  }

  /**
   * Get the path of the directory which stores the index file
   * 
   * @return the path of the directory which stores the index file
   */
  public Directory getIndexDir() {
    return indexDir;
  }

  /**
   * clear all the index files in the Lucence directory
   * 
   * @param indexDir - Lucene directory to store index file
   * @throws IngestionException
   */
  private void clearIndexDirectory(Directory indexDir) throws IngestionException {
    if (indexDir != null) {
      try {
        String[] files = indexDir.listAll();
        for (String file : files)
          indexDir.deleteFile(file);
      } catch (IOException e) {
        throw new IngestionException(e);
      }
    }
  }

  /**
   * save the index statistics file into a disk folder
   * 
   * @throws IngestionException
   */
  private void saveIndexStatToDisk() throws IngestionException {
    PrintWriter out;
    try {
      File txtFile = new File(indexStatPath);
      if (txtFile.getParentFile() != null)
        txtFile.getParentFile().mkdirs();
      if (!txtFile.exists())
        txtFile.createNewFile();
      out = new PrintWriter(txtFile);
      out.println(indexStat.toString());
      out.close();
    } catch (IOException e) {
      throw new IngestionException(e);
    }
  }

  public String getIndexStatPath() {
    return indexStatPath;
  }

  public IndexingStats getIndexStat() {
    return indexStat;
  }

}
