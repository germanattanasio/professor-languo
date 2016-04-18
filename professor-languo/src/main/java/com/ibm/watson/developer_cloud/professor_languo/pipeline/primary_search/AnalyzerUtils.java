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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * AnalyzerUtils is a utility class which provides the methods needed to break down the input string
 * into pieces and construct the query with those pieces.
 * 
 */
public final class AnalyzerUtils {
  /**
   * Tokenize the text with the way specified in the analyzer and return the list of tokens
   * 
   * @param analyzer - lucene analyzer to tokenize the text
   * @param text - text to be tokenized by the analyzer
   * @return the list of the tokens for that text
   * @throws IOException
   */
  public static List<String> collectTokens(Analyzer analyzer, String text) throws IOException {
    List<String> result = new ArrayList<String>();

    try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))) {
      CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while (stream.incrementToken()) {
        result.add(termAtt.toString());
      }
      stream.end();
    }
    return result;
  }

}
