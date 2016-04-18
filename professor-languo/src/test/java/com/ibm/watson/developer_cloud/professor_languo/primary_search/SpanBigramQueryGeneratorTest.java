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

package com.ibm.watson.developer_cloud.professor_languo.primary_search;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.PrimarySearchConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.QueryComponentFactory;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.SingletonAnalyzer;

public class SpanBigramQueryGeneratorTest {
  SpanBigramQueryGeneratorTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private Question queryQuestion = null;
  private Query spanBigramQuery = null;

  private final String question = "The quick brown fox jumped over the lazy dogs";

  @Test public void test_bigram_query_is_generated_correctly() throws IOException, SearchException, IngestionException {
    GIVEN.query_question_is_built();
    WHEN.bigram_query_is_generated();
    THEN.test_that_generated_bigram_query_match_the_referenced_query();
  }

  private void bigram_query_is_generated() throws SearchException, IngestionException {
    if (spanBigramQuery == null) {
      SingletonAnalyzer.generateAnalyzer(PrimarySearchConstants.ENGLISH_ANALYZER);
      spanBigramQuery = QueryComponentFactory.generateQueryComponent(PrimarySearchConstants.SPAN_BIGRAM_QUERY_GENERATOR)
          .generateQuery(queryQuestion);
    }
  }

  private void test_that_generated_bigram_query_match_the_referenced_query() throws IOException {
    // Get stemmed question
    SingletonAnalyzer.generateAnalyzer(PrimarySearchConstants.ENGLISH_ANALYZER);
    EnglishAnalyzer ea = (EnglishAnalyzer) SingletonAnalyzer.getAnalyzer();
    TokenStream ts = ea.tokenStream("field", question);
    CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
    ts.reset();
    List<String> stemmedQuestion = new ArrayList<String>();
    while (ts.incrementToken())
      stemmedQuestion.add(charTermAttribute.toString());

    // get query terms
    BooleanClause[] clauses = ((BooleanQuery) spanBigramQuery).getClauses();
    SpanQuery[] queries;
    String term1, term2;
    List<String> unigrams = new ArrayList<String>();
    int numFields = clauses.length / (2 * stemmedQuestion.size() - 1);

    // test bigrams
    int bigramidx = 0;
    for (int idx = 0; idx < clauses.length; idx++) {
      Query q = clauses[idx].getQuery();
      if (q instanceof SpanNearQuery) {
        queries = ((SpanNearQuery) clauses[idx].getQuery()).getClauses();
        int termidx = bigramidx / numFields;
        term1 = ((SpanTermQuery) queries[0]).getTerm().text();
        term2 = ((SpanTermQuery) queries[1]).getTerm().text();
        assertEquals("Extracted first term doesn't match the stemmed term", stemmedQuestion.get(termidx), term1);
        assertEquals("Extracted second term doesn't match the stemmed term", stemmedQuestion.get(termidx + 1), term2);
        bigramidx++;
      } else if (q instanceof TermQuery) {
        unigrams.add(((TermQuery) clauses[idx].getQuery()).getTerm().text());
      } else {
        assertTrue("Unknown type of query found!", false);
      }
    }

    // test unigrams
    for (String s : unigrams)
      assertTrue(stemmedQuestion.contains(s));
    for (String s : stemmedQuestion)
      assertTrue(unigrams.contains(s));
  }

  private void query_question_is_built() {
    if (queryQuestion == null)
      queryQuestion = new StackExchangeQuestion(question, "", null, 1, "");
  }
}
