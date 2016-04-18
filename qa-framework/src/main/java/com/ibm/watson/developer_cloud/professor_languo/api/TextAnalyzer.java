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

package com.ibm.watson.developer_cloud.professor_languo.api;

import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;

/**
 * A TextAnalyzer takes text with some (or no) existing analysis and may add additional analysis.
 * For example, a named entity detector may require text that has been tokenized and parsed and add
 * detected named entities to the text.
 * 
 */
public interface TextAnalyzer extends QAComponent {

  public void process(TextWithAnalysis textWithAnalysis);

}
