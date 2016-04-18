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

package com.ibm.watson.developer_cloud.professor_languo.data_model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class is intended to store text plus some analysis of that text. For now the analysis is
 * just a list of tokens, and a collection of metadata key/value pairs, but this class may be
 * enhanced in the future to store results of parsing, entity detection, relation detection,
 * coreference resolution, etc.
 *
 */
public class TextWithAnalysis implements Serializable {

  private static final long serialVersionUID = 34412399564893479L;
  private String origText;
  private String parsedText;
  private List<Token> tokens;
  protected MetadataMap metadataMap = new MetadataMap();

  public TextWithAnalysis(String text) {
    this.origText = text;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = Collections.unmodifiableList(tokens);
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setParsedText(String text) {
    this.parsedText = text;
  }

  public String getParsedText() {
    return parsedText;
  }

  public String getText() {
    return origText;
  }

  public MetadataMap getMetadataMap() {
    return metadataMap;
  }
}
