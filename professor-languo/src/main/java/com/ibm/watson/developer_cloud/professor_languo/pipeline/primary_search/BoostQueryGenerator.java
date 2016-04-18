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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;

public abstract class BoostQueryGenerator implements QueryComponent {

  /**
   * Create a boost standard query for the {@link Question}. The boost standard query retrieves the
   * Lucene Documents whose {@link IndexDocumentFieldName#THREAD_TITLE}
   * {@link IndexDocumentFieldName#THREAD_TEXT} {@link IndexDocumentFieldName#THREAD_TAGS}
   * {@link IndexDocumentFieldName#ACCEPTED_ANSWER_TEXT}
   * {@link IndexDocumentFieldName#TOP_VOTED_ANSWER_TEXT} field contains any unigram token of the
   * {@link Question}'s title field. The {@link IndexDocumentFieldName#THREAD_TITLE} and
   * {@link IndexDocumentFieldName#THREAD_TAGS} are more important than other fields so they have
   * higher boost than others.
   * 
   * @param question - The {@link Question} feed from the pipeline
   * @return the Lucene {@link Query}
   * @throws SearchException
   */
  protected Query createBoostStandardQuery(Question question) throws SearchException {
    return createBoostQuery(question, SingletonAnalyzer.getAnalyzer());
  }

  /**
   * Create a boost ngram query for the {@link Question}. The boost standard query retrieves the
   * Lucene Documents whose {@link IndexDocumentFieldName#THREAD_TITLE}
   * {@link IndexDocumentFieldName#THREAD_TEXT} {@link IndexDocumentFieldName#THREAD_TAGS}
   * {@link IndexDocumentFieldName#ACCEPTED_ANSWER_TEXT}
   * {@link IndexDocumentFieldName#TOP_VOTED_ANSWER_TEXT} field contains any ngram token of the
   * {@link Question}'s title field. The {@link IndexDocumentFieldName#THREAD_TITLE} and
   * {@link IndexDocumentFieldName#THREAD_TAGS} are more important than other fields so they have
   * higher boost than others.
   * 
   * @param question - The {@link Question} feed from the pipeline
   * @return the Lucene {@link Query}
   * @throws SearchException
   */
  protected Query createBoostNgramQuery(Question question, int gap) throws SearchException {
    return createBoostQuery(question, new NgramAnalyzer(gap));
  }

  private Query createBoostQuery(Question question, Analyzer analyzer) throws SearchException {
    List<String> tokens;
    StackExchangeQuestion queryQuestion = (StackExchangeQuestion) question;
    String title = queryQuestion.getTitleText();
    List<TermQuery> termQueries = new ArrayList<TermQuery>();
    BooleanQuery query = new BooleanQuery();

    try {
      tokens = AnalyzerUtils.collectTokens(analyzer, title);
      for (String token : tokens) {
        TermQuery tqTitle = new TermQuery(new Term(IndexDocumentFieldName.THREAD_TITLE.toString(), token.trim()));
        tqTitle.setBoost(2);
        TermQuery tqText = new TermQuery(new Term(IndexDocumentFieldName.THREAD_TEXT.toString(), token.trim()));
        TermQuery tqAccAnswerText =
            new TermQuery(new Term(IndexDocumentFieldName.ACCEPTED_ANSWER_TEXT.toString(), token.trim()));
        TermQuery tqTopAnswerText =
            new TermQuery(new Term(IndexDocumentFieldName.TOP_VOTED_ANSWER_TEXT.toString(), token.trim()));
        TermQuery tqTags = new TermQuery(new Term(IndexDocumentFieldName.THREAD_TAGS.toString(), token.trim()));
        tqTags.setBoost(2);

        termQueries.add(tqTitle);// TODO:
        termQueries.add(tqText);
        termQueries.add(tqAccAnswerText);
        termQueries.add(tqTopAnswerText);
        termQueries.add(tqTags);
      }
      for (TermQuery q : termQueries)
        query.add(q, Occur.SHOULD);
    } catch (IOException e) {
      throw new SearchException(e);
    }
    return query;
  }

}
