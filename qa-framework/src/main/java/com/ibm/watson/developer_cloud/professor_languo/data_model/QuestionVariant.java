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

/**
 * Question variant is a class to store the question variants as objects, with qid and text as
 * attributes
 * 
 */
public class QuestionVariant {
  private String qid;
  private String questionText;
  private String pauTID;

  public QuestionVariant(String qid, String questionText) {
    this.qid = qid;
    this.questionText = questionText;
  }

  public QuestionVariant(String qidTextString) {
    String[] qv = qidTextString.split(":", 2);
    if (qv.length == 2) {
      this.qid = qv[0].trim();
      this.questionText = qv[1].trim();
    }
  }

  public String getQid() {
    return qid;
  }

  public void setQid(String qid) {
    this.qid = qid;
  }

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public String getPauTID() {
    return pauTID;
  }

  public void setPauTID(String pauTID) {
    this.pauTID = pauTID;
  }

  public String getquesVarAsString() {
    return (qid + ":" + questionText);
  }

  @Override public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final QuestionVariant other = (QuestionVariant) obj;
    boolean sameQID = (this.qid != null && this.qid.equalsIgnoreCase(other.qid));
    if (sameQID)
      return true;
    boolean sameQText = (this.questionText != null && this.questionText.equalsIgnoreCase(other.questionText));
    if (sameQText)
      return true;
    return false;
  }

  @Override public int hashCode() {
    int hash = 3;
    hash = 89 * hash + (this.qid == null ? 0 : this.qid.toUpperCase().hashCode());
    hash = 89 * hash + (this.questionText == null ? 0 : this.questionText.toUpperCase().hashCode());
    return hash;
  }


}
