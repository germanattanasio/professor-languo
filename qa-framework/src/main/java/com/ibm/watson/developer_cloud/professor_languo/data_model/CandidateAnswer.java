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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.watson.developer_cloud.professor_languo.api.TextAnalyzer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;

/**
 * Candidate answers to questions, with features and numerical values for those features. Also
 * includes relevant search text, e.g., document text for an answer whose label is a docId, or
 * mapped question variants for an answer that was retrieved by TAO-style question-store search.
 * 
 */
public class CandidateAnswer implements Comparable<CandidateAnswer>, Serializable {

  private static final long serialVersionUID = 3970145547097238116L;

  private static int currentId = 0;

  private int id;
  private String answerLabel;
  private String componentId;
  private Double confidence = null;

  private Map<String, Double> featureValuePairs = new LinkedHashMap<>();

  protected TextWithAnalysis answerDocumentText = null;
  protected TextWithAnalysis answerTitle = null;
  protected Collection<TextWithAnalysis> questionVariants = null;
  private static final Logger logger = LogManager.getLogger();

  /**
   * A map of metadata key/value pairs associated with this answer
   */
  private MetadataMap metadataMap = new MetadataMap();

  /**
   * Discard the search results for this answer. This is useful AFTER the search results are no
   * longer needed IF you are consuming too much memory in your application.
   */
  public void compact() {
    answerDocumentText = null;
    answerTitle = null;
    questionVariants = null;
    featureValuePairs = null;
  }

  /**
   * Runs text analysis on search results.
   * 
   * @param analyzers Search result processing components.
   */
  public void analyzeSearchResults(Collection<TextAnalyzer> analyzers) {
    if (answerDocumentText != null)
      logger.debug("Analyzing answerDocumentText");
    analyze(answerDocumentText, analyzers);
    if (answerTitle != null)
      logger.debug("Analyzing answerTitle");
    analyze(answerTitle, analyzers);
    if (questionVariants != null)
      logger.debug("Analyzing questionVariants text for " + questionVariants.size() + " variants");
    analyze(questionVariants, analyzers);
  }

  public TextWithAnalysis getAnswerDocumentText() {
    return answerDocumentText;
  }

  public void setAnswerDocumentText(TextWithAnalysis answerText) {
    this.answerDocumentText = answerText;
  }

  public TextWithAnalysis getAnswerTitle() {
    return answerTitle;
  }

  public void setAnswerTitle(TextWithAnalysis answerTitle) {
    this.answerTitle = answerTitle;
  }

  public Collection<TextWithAnalysis> getQuestionVariants() {
    if (questionVariants == null)
      return null;
    else
      return Collections.unmodifiableCollection(questionVariants);
  }

  public void setQuestionVariants(Collection<TextWithAnalysis> questionVariants) {
    this.questionVariants = questionVariants;
  }

  private void analyze(Collection<TextWithAnalysis> texts, Collection<TextAnalyzer> analyzers) {
    if (texts != null)
      for (TextWithAnalysis textWithAnalysis : texts) {
        analyze(textWithAnalysis, analyzers);
      }
  }

  private void analyze(TextWithAnalysis textWithAnalysis, Collection<TextAnalyzer> analyzers) {
    if (textWithAnalysis != null)
      for (TextAnalyzer analyzer : analyzers) {
        analyzer.process(textWithAnalysis);
      }
  }

  public CandidateAnswer(String answerLabel, String componentId) {
    this.answerLabel = answerLabel;
    this.componentId = componentId;
    id = newId();
  }

  private static synchronized int newId() {
    return currentId++;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getAnswerLabel() {
    return answerLabel;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public synchronized Double getFeatureValue(String featureLabel) {
    return featureValuePairs.get(featureLabel);
  }

  public synchronized void setFeatureValue(String featureLabel, Double value) {
    featureValuePairs.put(featureLabel, value);
  }

  public Set<Entry<String, Double>> getFeatureValuePairs() {
    return featureValuePairs.entrySet();
  }

  public MetadataMap getMetadataMap() {
    return metadataMap;
  }

  public void putMetadata(String key, Object value) {
    metadataMap.put(key, value);
  }

  @Override public boolean equals(Object o) {
    if (o instanceof CandidateAnswer)
      return id == ((CandidateAnswer) o).id;
    else
      return false;
  }

  @Override public int hashCode() {
    return (new Integer(id)).hashCode();
  }

  @Override public int compareTo(CandidateAnswer o) {
    double x = confidence == null ? 0d : confidence;
    double y = o.confidence == null ? 0d : o.confidence;

    if (x < y)
      return 1;
    else if (y < x)
      return -1;
    else
      return id - o.id;
  }

  public Set<String> getFeatures() {
    return featureValuePairs.keySet();
  }

  /**
   * Get the relevance score for the answer, given a list of known relevant answers with relevance
   * scores.
   * 
   * @param correctAnswers
   * @return
   */
  public int getRelevance(Collection<CorrectAnswer> correctAnswers) {
    int score = 0;
    if (correctAnswers != null)
      for (CorrectAnswer correctAnswer : correctAnswers) {
        if (correctAnswer.getText().equals(getAnswerLabel())) {
          int relevance = correctAnswer.getRelevance();
          if (relevance > score)
            score = relevance;
        }
      }
    return score;

  }

  @Override public String toString() {
    StringBuffer b = new StringBuffer("{");
    b.append(getAnswerLabel());
    b.append(':');
    b.append(getComponentId());
    b.append("[ ");
    for (Entry<String, Double> e : featureValuePairs.entrySet()) {
      b.append(e.getKey());
      b.append('=');
      b.append(e.getValue());
      b.append(' ');
    }
    b.append("]");
    b.append("[ ");
    for (Entry<String, Object> e : metadataMap.entrySet()) {
      b.append(e.getKey());
      b.append("=\"");
      b.append(e.getValue());
      b.append("\" ");
    }
    b.append("]}");
    return b.toString();
  }

}
