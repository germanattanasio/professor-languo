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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * <tt>QuestionAnswerSet</tt> manages a set of questions and their answers. A question has an id and
 * question text. Each question may have zero or more than one answer. This class is mainly designed
 * to keep all the questions and answers in a set, say, a training or test set.
 * 
 */
public class QuestionAnswerSet {

  public static class CorrectAnswer {
    String answerText;
    int answerRelevance = 1;
    public boolean isRegexMatch = false;

    public CorrectAnswer(String answerText) {
      this.answerText = answerText;
    }

    public CorrectAnswer(String answerText, int relevance) {
      this.answerText = answerText;
      this.answerRelevance = relevance;
    }

    public CorrectAnswer(String answerText, boolean regexMatch) {
      this.answerText = answerText;
      this.isRegexMatch = regexMatch;
    }

    public String getText() {
      return answerText;
    }

    public int getRelevance() {
      return answerRelevance;
    }

    public static boolean isCorrect(CandidateAnswer answer, Collection<CorrectAnswer> correctAnswers) {
      return isCorrect(answer, correctAnswers, 1);
    }

    public static boolean isCorrect(CandidateAnswer answer, Collection<CorrectAnswer> correctAnswers,
        int minRelevance) {

      for (CorrectAnswer c : correctAnswers) {
        if (c.isRegexMatch) {
          if (answer.getAnswerLabel().matches(c.answerText)) {
            return true;
          }
        } else {
          if ((c.answerRelevance >= minRelevance) && (c.answerText.equals(answer.getAnswerLabel()))) {
            return true;
          }
        }
      }
      return false;
    }
  }

  /**
   * The map that maps question id to question text.
   */
  final Map<String, Question> qidToQuestion;

  /**
   * The map that maps question id to the answers.
   */
  final Multimap<String, CorrectAnswer> qidToAnswers;

  /**
   * The set of all the answers.
   */
  final Set<CorrectAnswer> answers;
  String commonEng;
  String source;

  // Use the same random seed for every instance, so that we get comparable results when we run
  // multiple times.
  private static final Random random = new Random(10598l);

  public String getCommonEng() {
    return commonEng;
  }

  public void setCommonEng(String commonEng) {
    this.commonEng = commonEng;
  }

  public String getSource() {
    return source;
  }

  /**
   * Constructs an empty <tt>QuestionAnswerSet</tt>.
   */
  public QuestionAnswerSet(String source) {
    qidToAnswers = HashMultimap.create();
    qidToQuestion = new LinkedHashMap<>(); // keep insertion order
    answers = new HashSet<>();
    this.source = source;
  }

  /**
   * Add a specified question answer pair. The answers to the question can be <code>null</code> or
   * empty if the question belongs to the test set. This method returns <code>false</code> only when
   * the question text or id is <code>null</code>. If a question with the same id already exists, a
   * {@link RuntimeException} is thrown
   * 
   * @param qid the id of the question to be added
   * @param engagement the engagement for this question set
   * @param questionText the text of the question to be added
   * @param answers the answers to the question to be added
   * @return <code>true</code> if the question is added
   */
  public boolean addQuestionAnswers(String qid, String engagement, String questionText,
      Collection<CorrectAnswer> answers) {
    return addQuestionAnswers(qid, engagement, questionText, answers, null);
  }

  /**
   * Add a specified question answer pair. The answers to the question can be <code>null</code> or
   * empty if the question belongs to the test set. This method returns <code>false</code> only when
   * the question text or id is <code>null</code>. If a question with the same id already exists, a
   * {@link RuntimeException} is thrown
   * 
   * @param qid the id of the question to be added
   * @param engagement the engagement for this question set
   * @param questionText the text of the question to be added
   * @param answers the answers to the question to be added
   * @param metadata Metadata that should be added to the question
   * @return <code>true</code> if the question is added
   */
  public boolean addQuestionAnswers(String qid, String engagement, String questionText,
      Collection<CorrectAnswer> answers, Map<String, Object> metadata) {
    Question question = new Question(questionText, engagement, qid, source);
    return addQuestionAnswers(question, answers, metadata);
  }

  /**
   * Add a specified question answer pair. The answers to the question can be <code>null</code> or
   * empty if the question belongs to the test set. This method returns <code>false</code> only when
   * the question text or id is <code>null</code>. If a question with the same id already exists, a
   * {@link RuntimeException} is thrown
   * 
   * @param question - The {@link Question} being added to this QA set
   * @param answers - A collection of {@link CorrectAnswer} objects to associate with this question
   * @param metadata - Metadata that should be added to the question
   * 
   * @return <code>true</code> if the question is added
   */
  public boolean addQuestionAnswers(Question question, Collection<CorrectAnswer> answers,
      Map<String, Object> metadata) {
    String qid = question.getId();
    if (qid == null || question.getText() == null)
      return false;

    if (metadata != null) {
      for (String eachKey : metadata.keySet()) {
        question.putMetadata(eachKey, metadata.get(eachKey));
      }
    }

    if (qidToQuestion.containsKey(qid))
      throw new RuntimeException("Duplicate QID: " + qid);

    qidToQuestion.put(qid, question);
    if (answers != null) {
      for (CorrectAnswer answer : answers) {
        qidToAnswers.put(qid, answer);
        this.answers.add(answer);
      }
    }

    return true;
  }

  /**
   * Returns all the answers in the question answer set.
   * 
   * @return all the answers in the question answer set
   */
  public Set<CorrectAnswer> getAnswers() {
    return answers;
  }

  /**
   * Returns the answers to the specified question.
   * 
   * @param qid the id of the question
   * @return the answers to the question
   */
  public Collection<CorrectAnswer> getAnswers(String qid) {
    return qidToAnswers.get(qid);
  }

  /**
   * Returns the question text of the specified question.
   * 
   * @param qid the id of the question
   * @return the text of the question
   */
  public Question getQuestion(String qid) {
    return qidToQuestion.get(qid);
  }

  public Collection<Question> getQuestions() {
    return qidToQuestion.values();
  }

  /**
   * Returns the ids of all the questions.
   * 
   * @return the ids of all the questions
   */
  public Set<String> getQuestionIds() {
    return qidToQuestion.keySet();
  }

  /**
   * Returns the number of questions.
   * 
   * @return the number of questions
   */
  public int size() {
    return qidToQuestion.size();
  }

  /**
   * Divides up a train set into folds, for cross-fold validation and/or cross-fold ensembling. If
   * the number of folds is less than or equal to 0, then this generates a single fold with all of
   * the questions being both in AND out of the fold (e.g., the set becomes both train and test for
   * cross-fold validation). Otherwise, numFolds are generated and for each fold every question in
   * the input set is either in the fold or out of the fold (and none is in both). Questions are
   * assigned randomly to folds. All questions are in exactly one fold.
   * 
   * @param numFolds Number of folds if positive. Otherwise, produce one superfold with all
   *        questions in AND out.
   * @return A list of folds.
   */
  public List<Fold> fold(int numFolds) {
    List<Fold> retval = new ArrayList<Fold>();
    if (numFolds <= 0) {
      retval.add(new Fold(this, this));
    } else {
      List<Question> questions = new ArrayList<>(getQuestions());
      Collections.shuffle(questions, random);
      int numQuestions = questions.size();
      int foldSize = numQuestions / numFolds;
      List<List<Question>> allFoldQuestions = Lists.partition(questions, foldSize);
      for (List<Question> inFold : allFoldQuestions) {
        List<Question> outOfFold = new ArrayList<>(getQuestions());
        outOfFold.removeAll(inFold);
        Fold fold = new Fold(makeSet(inFold, source), makeSet(outOfFold, source));
        retval.add(fold);
      }
    }

    return retval;
  }

  private QuestionAnswerSet makeSet(Collection<Question> qs, String source) {
    QuestionAnswerSet retval = new QuestionAnswerSet(source);
    for (Question q : qs) {
      retval.addQuestionAnswers(q.getId(), q.getEngagement(), q.getText(), getAnswers(q.getId()));
    }
    return retval;
  }

  public static class Fold {
    final QuestionAnswerSet questionsInFold;
    final QuestionAnswerSet questionsOutOfFold;

    public Fold(QuestionAnswerSet questionsInFold, QuestionAnswerSet questionsOutOfFold) {
      this.questionsInFold = questionsInFold;
      this.questionsOutOfFold = questionsOutOfFold;
    }

    public QuestionAnswerSet getQuestionsInFold() {
      return questionsInFold;
    }

    public QuestionAnswerSet getQuestionsOutOfFold() {
      return questionsOutOfFold;
    }

  }

}
