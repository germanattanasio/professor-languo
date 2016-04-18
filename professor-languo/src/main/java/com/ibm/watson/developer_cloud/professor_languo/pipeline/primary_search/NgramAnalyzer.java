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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

public class NgramAnalyzer extends StopwordAnalyzerBase {

  private int gap = 2;// bi-gram by default

  public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

  public NgramAnalyzer(CharArraySet stopWords) {
    super(stopWords);
  }

  public NgramAnalyzer(int gap, CharArraySet stopWords) {
    super(stopWords);
    this.gap = gap;
  }

  public NgramAnalyzer() {
    super(STOP_WORDS_SET);
  }

  public NgramAnalyzer(int gap) {
    super(STOP_WORDS_SET);
    this.gap = gap;
  }

  @Override protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new StandardTokenizer();
    ShingleFilter sf =
        new ShingleFilter(new StopFilter(new LowerCaseFilter(new StandardFilter(tokenizer)), STOP_WORDS_SET));
    sf.setMaxShingleSize(gap);
    // sf.setFillerToken("");
    // sf.setOutputUnigrams(false);
    return new TokenStreamComponents(tokenizer, sf);
  }

}
