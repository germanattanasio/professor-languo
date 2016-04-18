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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineConstants;

public class LuceneSearcher implements Searcher {
  private IndexReader indexReader = null;
  private IndexSearcher searcher = null;

  /** by default pick out the top 20 candidate answers after the search **/
  private int candidateAnswerNum;

  /**
   * Creates a new {@link LuceneSearcher} that is <em>not</em> initialized.
   * 
   * @throws SearchException
   */
  public LuceneSearcher() throws SearchException {
    super();
  }

  @Override public void initialize(Properties properties) throws SearchException {
    String resDirPath = properties.getProperty(ConfigurationConstants.INGESTION_BASE_DIR) + File.separator;
    if (properties.getProperty(ConfigurationConstants.INDEX_DIR_TYPE)
        .equals(ConfigurationConstants.IndexDirTypes.FS.toString())) {
      String indexDirPath = resDirPath + properties.getProperty(ConfigurationConstants.INDEX_DIR);
      try {
        Directory indexDir = FSDirectory.open(new File(indexDirPath).toPath());
        indexReader = DirectoryReader.open(indexDir);
        searcher = new IndexSearcher(indexReader);
      } catch (IOException e) {
        throw new SearchException(e);
      }
    } else {
      throw new SearchException(Messages.getString("RetrieveAndRank.LUCENE_SEARCHER_INIT")); //$NON-NLS-1$
    }
    candidateAnswerNum = Integer.parseInt(properties.getProperty(ConfigurationConstants.CANDIDATE_ANSWER_NUM));
  }

  @Override public Set<CandidateAnswer> performSearch(Query query) throws SearchException {
    TopScoreDocCollector collector = TopScoreDocCollector.create(candidateAnswerNum);
    try {
      searcher.search(query, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      Set<CandidateAnswer> candidateAnswers = new HashSet<CandidateAnswer>();
      int rank = 1;
      for (ScoreDoc scoreDoc : hits) {
        Document doc = searcher.doc(scoreDoc.doc);
        byte[] binCode = doc.getBinaryValue(IndexDocumentFieldName.SERIALIZED_THREAD.toString()).bytes;
        CandidateAnswer candidateAnswer = StackExchangeThreadSerializer.deserializeThreadFromBinArr(binCode);
        candidateAnswer.setFeatureValue(PipelineConstants.FEATURE_SEARCH_SCORE, (double) scoreDoc.score);
        candidateAnswer.setFeatureValue(PipelineConstants.FEATURE_SEARCH_RANK, (double) rank++);
        candidateAnswers.add(candidateAnswer);
      }
      return candidateAnswers;

    } catch (IOException | IngestionException e) {
      throw new SearchException(e);
    }
  }

  /**
   * Close the index reader after all the search action have been performed
   * 
   * @throws IOException
   */
  public void closeIndexReader() throws IOException {
    indexReader.close();
  }

  /**
   * @deprecated - should only be used in the Unit Tests<br>
   *             Create a new instance of the {@link LuceneSearcher}
   * @param indexDir - The Directory that stores the index file
   * @throws IOException
   */
  public LuceneSearcher(Directory indexDir, int searchResultSize) throws SearchException {
    super();
    this.initialize(indexDir);
    this.candidateAnswerNum = searchResultSize;
  }

  /**
   * @deprecated - should only be used in the Unit Tests<br>
   *             Perform the search operation for the query from the user.
   * @param queryString - query String from the user of the Lucene
   * @return A list of the candidate answers
   * @throws SearchException
   */
  public Set<StackExchangeThread> performSearch(String queryString) throws SearchException {
    Set<StackExchangeThread> result = new HashSet<StackExchangeThread>();
    try {
      QueryParser parser =
          new QueryParser(IndexDocumentFieldName.THREAD_TITLE.toString(), SingletonAnalyzer.getAnalyzer());
      Query query = parser.parse(queryString);
      Set<CandidateAnswer> searchResult = performSearch(query);
      for (CandidateAnswer answer : searchResult)
        result.add((StackExchangeThread) answer);
    } catch (ParseException e) {
      throw new SearchException(e);
    }
    return result;
  }

  /**
   * @deprecated - should only be used in the Unit Tests<br>
   *             Initialize this {@link LuceneSearcher} using a given Lucene directory
   *
   * @param indexDir - The {@link Directory} containing the index
   * @throws SearchException
   */
  private void initialize(Directory indexDir) throws SearchException {
    try {
      indexReader = DirectoryReader.open(indexDir);
      searcher = new IndexSearcher(indexReader);
    } catch (IOException e) {
      throw new SearchException(e);
    }
  }
}
