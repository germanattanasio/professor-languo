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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.CorrectAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet.Fold;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A question answerer takes a question and provides a ranked list of answers with confidence
 * scores.
 * 
 */
public abstract class PipelineQuestionAnswerer implements QuestionAnswerer {

  private boolean filterDups;
  private List<TextAnalyzer> questionAnalysisComponents = new ArrayList<>();
  private List<TextAnalyzer> searchResultProcessingComponents = new ArrayList<>();
  private List<EvidenceRetriever> evidenceRetriverComponents = new ArrayList<>();
  private List<AnswerGenerator> answerGenerationComponents = new ArrayList<>();
  private List<AnswerScorer> answerScorerComponents = new ArrayList<>();
  private List<AnswerMergerAndRanker> answerMergerAndRankerComponents = new ArrayList<>();
  private List<AnswerPostprocessor> answerPostprocessorComponents = new ArrayList<>();
  private Set<TrainableComponent> trainableComponents = new HashSet<>();

  private static final Logger logger = LogManager.getLogger();
  public static final String PROP_NUM_FOLDS = "PipelineQuestionAnswerer.numFolds";
  public static final String PROP_NUM_THREADS = "PipelineQuestionAnswerer.numThreads";
  static final String PROP_FILTER_DUPS = "PipelineQuestionAnswerer.filterDuplicates";
  int numFolds = -1;
  private Scheduler scheduler;
  private ExecutorService executor;

  @Override public Observable<List<CandidateAnswer>> answer(final Question question,
      final Collection<CorrectAnswer> correctAnswers) {
    Observable<List<CandidateAnswer>> retval = process(question, null);
    retval = retval.map(new Func1<List<CandidateAnswer>, List<CandidateAnswer>>() {
      @Override public List<CandidateAnswer> call(List<CandidateAnswer> answers) {
        for (AnswerPostprocessor answerPostprocessor : answerPostprocessorComponents) {
          answerPostprocessor.postprocessAnswers(question, Observable.from(answers), correctAnswers);
        }
        for (CandidateAnswer answer : answers) {
          // discard search results for this answer because we are done processing it and we need to
          // free up memory
          answer.compact();
        }
        return answers;
      }
    });

    return retval;
  }

  @Override public void finish() {
    for (AnswerPostprocessor answerPostprocessor : answerPostprocessorComponents) {
      answerPostprocessor.finishPostprocessing();
    }
    executor.shutdown();
  }

  @Override public void train(QuestionAnswerSet trainSet, QuestionAnswerSet componentTraining) {
    if (componentTraining == null) {
      trainWithoutComponentTrainingData(trainSet);
    } else {
      trainWithComponentTrainingData(trainSet, componentTraining);
    }
  }

  public Observable<AnsweredQuestion> apply(final QuestionAnswerSet testSet) {
    Observable<Question> questionStream = Observable.from(testSet.getQuestions());

    Observable<AnsweredQuestion> testAnswers =
        scheduleMap(scheduler, questionStream, new Func1<Question, AnsweredQuestion>() {
          @Override public AnsweredQuestion call(Question question) {
            logger.info(question.getId());
            Collection<CorrectAnswer> correctAnswers = testSet.getAnswers(question.getId());
            Observable<List<CandidateAnswer>> candidates = answer(question, correctAnswers);
            AnsweredQuestion retval = new AnsweredQuestion(question, candidates.toBlocking().first());
            logger.trace(retval.getAnswers());
            return retval;
          }
        });

    return testAnswers;
  }

  private void trainWithComponentTrainingData(QuestionAnswerSet trainSet, QuestionAnswerSet componentTraining) {
    if (numFolds > 0) {
      throw new RuntimeException(
          "Cannot run with both a component training set and multiple training folds.  This combination would not make sense because");
    }
    Fold fold = new Fold(trainSet, componentTraining);
    List<Fold> folds = new ArrayList<>();
    folds.add(fold);
    train(folds, trainSet);
  }

  private void trainWithoutComponentTrainingData(QuestionAnswerSet trainSet) {
    List<Fold> folds = trainSet.fold(numFolds);
    train(folds, trainSet);
  }

  private void train(final List<Fold> folds, final QuestionAnswerSet trainSet) {
    logger.info("number of folds:" + folds.size());
    logger.info("trainable components:" + trainableComponents);

    for (Fold fold : folds) {
      // train the subcomponents on the questions that are NOT in the fold
      for (TrainableComponent trainable : trainableComponents) {
        trainable.train(fold.getQuestionsOutOfFold());
      }
      // train the pipeline on the questions that are in the fold, with the subcomponents trained
      // on questions not in the fold
      Observable<Question> questionStream = Observable.from(fold.getQuestionsInFold().getQuestions());

      Observable<List<CandidateAnswer>> trainAnswers =
          scheduleMap(scheduler, questionStream, new Func1<Question, List<CandidateAnswer>>() {
            @Override public List<CandidateAnswer> call(Question question) {
              logger.info(question.getId());
              Collection<CorrectAnswer> correctAnswers = trainSet.getAnswers(question.getId());
              Observable<List<CandidateAnswer>> candidates = train(question, correctAnswers);
              return candidates.toBlocking().first();
            }
          });

      // block until all the training runs for the fold are complete, because we are about to
      // retrain the subcomponents.
      trainAnswers.subscribeOn(scheduler).toBlocking().lastOrDefault(null);
    }

    // Complete training for the pipeline
    for (AnswerMergerAndRanker answerMergerAndRanker : answerMergerAndRankerComponents) {
      answerMergerAndRanker.finishTraining();
    }

    // Now retrain all the subcomponents using all of the training set, so they are ready
    // to be used on the validation and test sets.
    if (folds.size() > 1) {
      for (TrainableComponent trainable : trainableComponents) {
        trainable.train(trainSet);
      }
    }
  }

  private Observable<List<CandidateAnswer>> train(final Question question,
      final Collection<CorrectAnswer> correctAnswers) {
    return process(question, correctAnswers);
  }

  /**
   * @param question A question to be answered.
   * @param correctAnswers In training mode, this is the collection of correct answers to the
   *        question. In answering mode, this is null.
   * @return Answers to the question. In answering mode, these should have meaningful confidence
   *         scores.
   */
  private Observable<List<CandidateAnswer>> process(final Question question,
      final Collection<CorrectAnswer> correctAnswers) {

    for (TextAnalyzer analyzer : questionAnalysisComponents) {
      analyzer.process(question);
    }

    Collection<Observable<CandidateAnswer>> candidateAnswerStreams = new ArrayList<>(answerGenerationComponents.size());
    for (AnswerGenerator answerGenerator : answerGenerationComponents) {
      Observable<CandidateAnswer> candidateAnswersForGenerator = answerGenerator.generateCandidateAnswers(question);
      candidateAnswerStreams.add(candidateAnswersForGenerator);
    }
    Observable<CandidateAnswer> candidateAnswers = Observable.merge(candidateAnswerStreams);
    final Collection<String> observedKeys = new HashSet<>();
    candidateAnswers = candidateAnswers.map(new Func1<CandidateAnswer, CandidateAnswer>() {
      @Override public CandidateAnswer call(CandidateAnswer answer) {
        if (!filterDuplicate(answer, observedKeys)) {
          for (EvidenceRetriever retriever : evidenceRetriverComponents) {
            retriever.findEvidence(question, answer);
          }
          answer.analyzeSearchResults(searchResultProcessingComponents);
          for (AnswerScorer scorer : answerScorerComponents) {
            scorer.scoreCandidateAnswer(question, answer);
          }
        }
        return answer;
      }
    });

    Observable<CandidateAnswer> existingAnswers = candidateAnswers;
    Observable<CandidateAnswer> rankedAnswers = null;
    for (AnswerMergerAndRanker answerMergerAndRanker : answerMergerAndRankerComponents) {
      rankedAnswers = answerMergerAndRanker.mergeAndRankAnswers(question, existingAnswers, correctAnswers);
      existingAnswers = rankedAnswers;
    }

    return rankedAnswers == null ? null : rankedAnswers.toSortedList();
  }

  private synchronized boolean filterDuplicate(CandidateAnswer answer, Collection<String> observedKeys) {
    if (filterDups) {
      String label = answer.getAnswerLabel();
      if (observedKeys.contains(label))
        return true;
      else {
        observedKeys.add(label);
        return false;
      }
    } else {
      return false;
    }
  }

  protected void addQuestionAnalysisComponent(TextAnalyzer questionAnalysisComponent) {
    questionAnalysisComponents.add(questionAnalysisComponent);
    if (questionAnalysisComponent instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) questionAnalysisComponent);
  }

  protected void addAnswerGenerationComponent(AnswerGenerator answerGenerationComponent) {
    answerGenerationComponents.add(answerGenerationComponent);
    if (answerGenerationComponent instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) answerGenerationComponent);
  }

  protected void addSearchResultProcessingComponent(TextAnalyzer searchResultProcessingComponent) {
    searchResultProcessingComponents.add(searchResultProcessingComponent);
    if (searchResultProcessingComponent instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) searchResultProcessingComponent);
  }

  protected void addAnswerScorerComponent(AnswerScorer answerScorerComponent) {
    answerScorerComponents.add(answerScorerComponent);
    if (answerScorerComponent instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) answerScorerComponent);
  }

  protected void addAnswerMergerAndRankerComponent(AnswerMergerAndRanker answerMergerAndRanker) {
    answerMergerAndRankerComponents.add(answerMergerAndRanker);
    if (answerMergerAndRanker instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) answerMergerAndRanker);
  }

  protected void addAnswerPostprocessorComponent(AnswerPostprocessor answerPostprocessor) {
    answerPostprocessorComponents.add(answerPostprocessor);
    if (answerPostprocessor instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) answerPostprocessor);
  }

  protected void addEvidenceRetriverComponent(EvidenceRetriever evidenceRetriverComponent) {
    evidenceRetriverComponents.add(evidenceRetriverComponent);
    if (evidenceRetriverComponent instanceof TrainableComponent)
      trainableComponents.add((TrainableComponent) evidenceRetriverComponent);
  }

  @Override public void initialize(Properties properties) {
    int nThreads = getRequiredInteger(properties, PROP_NUM_THREADS);

    filterDups = getOptionalBoolean(properties, PROP_FILTER_DUPS, false);

    logger.info("Initializing scheduler with " + nThreads + " threads.");
    executor = Executors.newFixedThreadPool(nThreads);
    scheduler = Schedulers.from(executor);

    numFolds = Integer.parseInt(properties.getProperty(PROP_NUM_FOLDS, "-1"));
    initialize(questionAnalysisComponents, properties);
    initialize(answerGenerationComponents, properties);
    initialize(evidenceRetriverComponents, properties);
    initialize(searchResultProcessingComponents, properties);
    initialize(answerScorerComponents, properties);
    initialize(answerMergerAndRankerComponents, properties);
    initialize(answerPostprocessorComponents, properties);
  }

  private void initialize(List<? extends QAComponent> comps, Properties properties) {
    for (QAComponent comp : comps)
      comp.initialize(properties);

  }

  private static <InType, OutType> Observable<OutType> scheduleMap(final Scheduler scheduler,
      final Observable<InType> inputStream, final Func1<InType, OutType> computation) {
    Func1<InType, Observable<OutType>> observableComputation = new Func1<InType, Observable<OutType>>() {
      @Override public Observable<OutType> call(InType arg) {
        return Observable.just(arg).map(computation).subscribeOn(scheduler);
      }
    };
    return inputStream.flatMap(observableComputation);
  }

  private static int getRequiredInteger(Properties props, String prop) {
    String retval = props.getProperty(prop);
    if (retval == null)
      throw new RuntimeException("Required property not set in properties file: " + prop);
    return Integer.parseInt(retval);
  }

  private static boolean getOptionalBoolean(Properties props, String prop, boolean defaultVal) {
    String s = props.getProperty(prop);
    if (s == null)
      return defaultVal;
    try {
      return Boolean.parseBoolean(s);
    } catch (Exception e) {
      throw new RuntimeException("Boolean property does not have boolean value: " + prop + "=" + s);
    }
  }
}
