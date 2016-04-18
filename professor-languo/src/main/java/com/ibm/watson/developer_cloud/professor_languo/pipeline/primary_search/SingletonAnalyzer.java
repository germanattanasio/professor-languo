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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;

public class SingletonAnalyzer {
  private static Analyzer INSTANCE;

  public static Analyzer getAnalyzer() {
    if (INSTANCE == null) {
      throw new NullPointerException(Messages.getString("RetrieveAndRank.ANALYZER_NULL")); //$NON-NLS-1$
    } else {
      return INSTANCE;
    }
  }

  public static Analyzer generateAnalyzer(String type) {
    Analyzer analyzer = null;
    switch (type) {
      case PrimarySearchConstants.STANDARD_ANALYZER:
        analyzer = new StandardAnalyzer();
        break;
      case PrimarySearchConstants.ENGLISH_ANALYZER:
        analyzer = new EnglishAnalyzer();
        break;
      default:
        analyzer = null;
        throw new IllegalArgumentException(Messages.getString("RetrieveAndRank.QUERY_GENERATOR_NOT_FOUND")); //$NON-NLS-1$
    }
    INSTANCE = analyzer;
    return analyzer;
  }

}
