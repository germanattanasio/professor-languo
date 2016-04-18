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

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;

/**
 * An evidence retriever takes an analyzed question and an answer and augments the answer with
 * additional information that may be useful for scoring. They run before both answer scoring AND
 * search result processing. They run before search result processing because they may add search
 * results.
 * 
 */
public interface EvidenceRetriever extends QAComponent {

  /**
   * Scores an existing candidate answer (by adding 0 or more feature-value pairs to the candidate
   * answer).
   * 
   * @param question the question to be answered
   * @param answer a candidate answer to be scored
   * @return a candidate answer that has been scored (typically this is the input answer with an
   *         additional feature added)
   */
  public CandidateAnswer findEvidence(Question question, CandidateAnswer answer);

}
