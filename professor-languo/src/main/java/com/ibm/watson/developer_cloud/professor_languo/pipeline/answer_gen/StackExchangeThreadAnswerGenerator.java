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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.answer_gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.Query;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.api.AnswerGenerator;
import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.QueryComponent;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.QueryComponentFactory;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.Searcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.SingletonAnalyzer;

/**
 * The {@link StackExchangeThreadAnswerGenerator} can generate a collection of candidate answers
 * given a {@link Question}.
 *
 */
public class StackExchangeThreadAnswerGenerator implements AnswerGenerator {

  private List<QueryComponent> queryComponents;
  private Searcher indexSearcher;

  public StackExchangeThreadAnswerGenerator() throws SearchException {
    super();
    this.queryComponents = new ArrayList<>();
  }

  public StackExchangeThreadAnswerGenerator(List<QueryComponent> queryComponents, Searcher indexSearcher) {
    super();
    this.queryComponents = queryComponents;
    this.indexSearcher = indexSearcher;
  }

  @Override public void initialize(Properties properties) {
    SingletonAnalyzer.generateAnalyzer(properties.getProperty(ConfigurationConstants.ANALYZER));
    String[] queryGenerators = properties.getProperty(ConfigurationConstants.QUERY_GENERATORS).split(",");
    int queryGeneratorNum = queryGenerators.length;
    try {
      for (int i = 0; i < queryGeneratorNum; i++) {
        String type = queryGenerators[i];
        QueryComponent queryComponent = QueryComponentFactory.generateQueryComponent(type);
        this.queryComponents.add(queryComponent);
      }
      if (this.indexSearcher == null) {
        this.indexSearcher = IndexerAndSearcherFactory.getSearcher(properties);
      }
    } catch (SearchException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public Observable<CandidateAnswer> generateCandidateAnswers(Question question) {
    List<CandidateAnswer> candidateAnswers = new ArrayList<>();

    // Iterate over each QueryComponent, searching the index for each query
    // and collecting all results
    try {
      for (QueryComponent q : this.queryComponents) {
        Query query = q.generateQuery(question);
        Collection<CandidateAnswer> answers = this.indexSearcher.performSearch(query);
        if (answers != null)
          candidateAnswers.addAll(answers);
      }
    } catch (SearchException e) {
      throw new RuntimeException(e);
    }

    mergeAnswers(candidateAnswers);

    return Observable.from(candidateAnswers);
  }

  private void mergeAnswers(Collection<? extends CandidateAnswer> candidateAnswers) {
    Set<Integer> uniquePostIds = new HashSet<>();

    // Iterate over all the candidate answers. If we find answers that share
    // the
    // same post ID, we will remove the duplicate answers from the
    // collection
    Iterator<? extends CandidateAnswer> it = candidateAnswers.iterator();
    while (it.hasNext()) {
      CandidateAnswer answer = it.next();
      if (answer instanceof StackExchangeThread) {
        StackExchangeThread thread = (StackExchangeThread) answer;
        if (!uniquePostIds.add(thread.getId())) {
          // This post ID was already added, thus remove this
          // additional answer
          it.remove();
        }
      } else
        throw new RuntimeException(Messages.getString("RetrieveAndRank.BAD_CANDIDATE_ANSWER")); //$NON-NLS-1$
    }
  }

}
