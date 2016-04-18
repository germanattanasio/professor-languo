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

public class Question extends TextWithAnalysis {

  private static final long serialVersionUID = 4089166761718889853L;
  private String id;
  /*
   * we need this engagement variable along with the question for this competition and may be some
   * others we need to know which engagement the question belongs to, and also need to append final
   * answer with the label
   */
  private String engagement;
  private String source;

  public Question(String text, String engagement, String id, String source) {
    super(text);
    this.engagement = engagement.toLowerCase();
    this.id = id;
    this.source = source;
  }


  public String getEngagement() {
    return engagement;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public void putMetadata(String key, Object value) {
    metadataMap.put(key, value);
  }
}
