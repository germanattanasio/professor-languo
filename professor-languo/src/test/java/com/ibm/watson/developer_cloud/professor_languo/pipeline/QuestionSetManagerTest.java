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

package com.ibm.watson.developer_cloud.professor_languo.pipeline;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.QuestionAnswerSet;
import com.ibm.watson.developer_cloud.professor_languo.exception.PipelineException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.CorpusBuilder;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.QuestionSetManager;

/**
 * The class <code>QuestionSetManagerTest</code> contains tests for the class {@link
 * <code>QuestionSetManager</code>}
 */
public class QuestionSetManagerTest {

  QuestionSetManagerTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private QuestionSetManager questionSetManager;

  private QuestionAnswerSet trainingSet, testSet, validationSet;

  private Set<String> duplicateQuestionIDs;

  private static File dupThreadTsvFile = null, trainFile = null, testFile = null, validateFile = null;

  static {
    dupThreadTsvFile = new File(QuestionSetManagerTest.class.getResource("/duplicateThreads/dup_thread.tsv").getPath());
    String basename = FilenameUtils.removeExtension(dupThreadTsvFile.getAbsolutePath());
    String extension = FilenameUtils.getExtension(dupThreadTsvFile.getAbsolutePath());
    trainFile = new File(basename + StackExchangeConstants.DUP_THREAD_TSV_TRAIN_FILE_SUFFIX
        + FilenameUtils.EXTENSION_SEPARATOR + extension);
    testFile = new File(basename + StackExchangeConstants.DUP_THREAD_TSV_TEST_FILE_SUFFIX
        + FilenameUtils.EXTENSION_SEPARATOR + extension);
    validateFile = new File(basename + StackExchangeConstants.DUP_THREAD_TSV_VALIDATE_FILE_SUFFIX
        + FilenameUtils.EXTENSION_SEPARATOR + extension);
  }

  @Test public void test_that_question_partitions_are_disjoint_1() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.7, 0.2, 0.1});
    AND.set_of_duplicate_question_ids_is_built();
    WHEN.partitions_are_retrieved();
    THEN.each_duplicate_question_belongs_to_a_single_partition();
  }

  @Test public void test_that_question_partitions_are_disjoint_2() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.8, 0.2, 0.0});
    AND.set_of_duplicate_question_ids_is_built();
    WHEN.partitions_are_retrieved();
    THEN.each_duplicate_question_belongs_to_a_single_partition();
  }

  @Test public void test_that_question_partitions_are_disjoint_3() throws PipelineException {
    GIVEN.question_set_manager_is_constructed_via_properties(new double[] {0.7, 0.2, 0.1});
    AND.set_of_duplicate_question_ids_is_built();
    WHEN.partitions_are_retrieved();
    THEN.each_duplicate_question_belongs_to_a_single_partition();
  }

  @Test public void test_that_question_partitions_yield_desired_ratios_1() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.7, 0.2, 0.1});
    AND.set_of_duplicate_question_ids_is_built();
    WHEN.partitions_are_retrieved();
    THEN.partition_ratios_are_correct(new double[] {0.7, 0.2, 0.1});
  }

  @Test public void test_that_question_partitions_yield_desired_ratios_2() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.8, 0.2, 0.0});
    AND.set_of_duplicate_question_ids_is_built();
    WHEN.partitions_are_retrieved();
    THEN.partition_ratios_are_correct(new double[] {0.8, 0.2, 0.0});
  }

  @Test public void test_that_partitions_created_with_the_same_random_seed_are_the_same() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.7, 0.2, 0.1});
    WHEN.partitions_are_retrieved();
    THEN.partitions_match_those_of_another_question_set_manager(new double[] {0.7, 0.2, 0.1});
  }

  @Test public void test_that_partitions_created_with_different_random_seed_are_different() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.7, 0.2, 0.1});
    WHEN.partitions_are_retrieved();
    THEN.partitions_are_different_for_differently_seeded_manager(new double[] {0.7, 0.2, 0.1});
  }

  @Test public void test_that_subset_tsv_files_are_properly_created() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.7, 0.2, 0.1});
    THEN.subset_tsv_files_are_properly_created();
  }

  @Test public void test_that_every_question_has_a_single_correct_answer() throws PipelineException {
    GIVEN.question_set_manager_is_constructed(new double[] {0.6, 0.2, 0.2});
    WHEN.partitions_are_retrieved();
    THEN.every_question_has_a_single_correct_answer();
  }

  @After public void tearDown() {
    // Remove the additional _train, _test, and _validate files that are
    // generated by the
    // QuestionSetManager
    if (trainFile.exists())
      trainFile.delete();
    if (testFile.exists())
      testFile.delete();
    if (validateFile.exists())
      validateFile.delete();
  }

  private void question_set_manager_is_constructed(double[] trainTestValidateProportions) throws PipelineException {
    this.questionSetManager = new QuestionSetManager(dupThreadTsvFile.getAbsolutePath(), trainTestValidateProportions);
  }

  private void question_set_manager_is_constructed_via_properties(double[] trainTestValidateProportions)
      throws PipelineException {
    Properties properties = new Properties();
    properties.setProperty(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, dupThreadTsvFile.getAbsolutePath());
    properties.setProperty(ConfigurationConstants.QUESTION_SET_MANAGER_PARTITION_FRACTIONS,
        "[" + trainTestValidateProportions[0] + ", " + trainTestValidateProportions[1] + ", "
            + trainTestValidateProportions[2] + "]");
    this.questionSetManager = QuestionSetManager.newInstance(properties);
  }

  private void set_of_duplicate_question_ids_is_built() throws PipelineException {
    // Compile a list of all duplicate thread QIDs from the TSV file
    this.duplicateQuestionIDs = new HashSet<>();
    try (CSVParser parser = CSVFormat.TDF.withHeader().parse(new FileReader(dupThreadTsvFile))) {
      for (CSVRecord record : parser.getRecords()) {
        duplicateQuestionIDs.add(record.get(CorpusBuilder.TSV_COL_HEADER_THREAD_ID));
      }
    } catch (IOException e) {
      throw new PipelineException(e);
    }
  }

  private void partitions_are_retrieved() {
    this.trainingSet = this.questionSetManager.getTrainingSet();
    this.testSet = this.questionSetManager.getTestSet();
    this.validationSet = this.questionSetManager.getValidationSet();
  }

  private void each_duplicate_question_belongs_to_a_single_partition() {
    // Grab the QIDs for each of the various subsets
    Set<String> trainIds = this.trainingSet.getQuestionIds();
    Set<String> testIds = this.testSet.getQuestionIds();
    Set<String> validateIds = this.validationSet.getQuestionIds();

    // Now check to make sure that a given QID belongs to one, and only one,
    // subset
    for (String threadId : this.duplicateQuestionIDs) {
      if (trainIds.contains(threadId)) {
        assertFalse(threadId + " belongs to both train and test sets", testIds.contains(threadId));
        assertFalse(threadId + " belongs to both train and validate sets", validateIds.contains(threadId));
      } else if (testIds.contains(threadId)) {
        assertFalse(threadId + " belongs to both test and train sets", trainIds.contains(threadId));
        assertFalse(threadId + " belongs to both test and validate sets", validateIds.contains(threadId));
      } else if (validateIds.contains(threadId)) {
        assertFalse(threadId + " belongs to both validate and train sets", trainIds.contains(threadId));
        assertFalse(threadId + " belongs to both validate and test sets", testIds.contains(threadId));
      } else
        fail(threadId + " did not belong to any partition");
    }
  }

  private void partition_ratios_are_correct(double[] trainTestValidateProportions) {
    // Verify that the actual size of each subset is (approximately) equal
    // to the expected size
    assertEquals("Training set size outside of allowable range",
        ((double) duplicateQuestionIDs.size()) * trainTestValidateProportions[0], (double) this.trainingSet.size(),
        (trainTestValidateProportions[0] == 0.0) ? 0.0 : 5.0);
    assertEquals("Test set size outside of allowable range",
        ((double) duplicateQuestionIDs.size()) * trainTestValidateProportions[1], (double) this.testSet.size(),
        (trainTestValidateProportions[1] == 0.0) ? 0.0 : 5.0);
    assertEquals("Validation set size outside of allowable range",
        ((double) duplicateQuestionIDs.size()) * trainTestValidateProportions[2], (double) this.validationSet.size(),
        (trainTestValidateProportions[2] == 0.0) ? 0.0 : 5.0);
  }

  private void partitions_match_those_of_another_question_set_manager(double[] trainTestValidateProportions)
      throws PipelineException {
    // Create another Manager to compare partitions against
    QuestionSetManager referenceManager =
        new QuestionSetManager(dupThreadTsvFile.getAbsolutePath(), trainTestValidateProportions);

    // Step through each of the QuestionAnswerSets, making sure the QIDs
    // contained in them
    // are equivalent between the two QuestionSetManagers
    QuestionAnswerSet set1 = null, set2 = null;
    for (int i = 1; i <= 3; i++) {
      switch (i) {
        // Decide which set we are using for this iteration
        case 1:
          set1 = this.questionSetManager.getTrainingSet();
          set2 = referenceManager.getTrainingSet();
          break;
        case 2:
          set1 = this.questionSetManager.getTestSet();
          set2 = referenceManager.getTestSet();
          break;
        case 3:
          set1 = this.questionSetManager.getValidationSet();
          set2 = referenceManager.getValidationSet();
          break;
      }

      assertTrue("Question sets aren't identical", set1.getQuestionIds().containsAll(set2.getQuestionIds())
          && set2.getQuestionIds().containsAll(set1.getQuestionIds()));
    }
  }

  private void partitions_are_different_for_differently_seeded_manager(double[] trainTestValidateProportions)
      throws PipelineException {
    // Create another Manager to compare partitions against, with a
    // different seed (42)
    QuestionSetManager referenceManager =
        new QuestionSetManager(dupThreadTsvFile.getAbsolutePath(), 42l, trainTestValidateProportions);

    QuestionAnswerSet set1 = null, set2 = null;
    for (int i = 1; i <= 3; i++) {
      switch (i) {
        // Decide which set we are using for this iteration
        case 1:
          set1 = this.questionSetManager.getTrainingSet();
          set2 = referenceManager.getTrainingSet();
          break;
        case 2:
          set1 = this.questionSetManager.getTestSet();
          set2 = referenceManager.getTestSet();
          break;
        case 3:
          set1 = this.questionSetManager.getValidationSet();
          set2 = referenceManager.getValidationSet();
          break;
      }

      assertFalse("Question sets are identical", set1.getQuestionIds().containsAll(set2.getQuestionIds())
          && set2.getQuestionIds().containsAll(set1.getQuestionIds()));
    }
  }

  private void subset_tsv_files_are_properly_created() throws PipelineException {
    // Confirm that TSV files of the same format as the duplicate thread TSV
    // file are created
    // for each of the subsets that are generated by the QuestionSetManager
    for (File file : Arrays.asList(trainFile, testFile, validateFile)) {
      assertTrue("File " + file.getName() + " is missing", file.exists());

      QuestionAnswerSet set;
      if (file == trainFile)
        set = this.questionSetManager.getTrainingSet();
      else if (file == testFile)
        set = this.questionSetManager.getTestSet();
      else
        set = this.questionSetManager.getValidationSet();

      // Iterate through each line of the subset TSV file and verify that
      // the records
      // it contains are in fact in the corresponding QuestionAnswerSet
      try (CSVParser parser = CSVFormat.TDF.withHeader().parse(new FileReader(file))) {
        for (CSVRecord record : parser.getRecords()) {
          assertTrue("Subset TSV file has erroneous QID",
              set.getQuestionIds().contains(record.get(CorpusBuilder.TSV_COL_HEADER_THREAD_ID)));
        }
      } catch (IOException e) {
        throw new PipelineException(e);
      }
    }
  }

  private void every_question_has_a_single_correct_answer() {
    for (QuestionAnswerSet set : Arrays.asList(this.trainingSet, this.testSet, this.validationSet)) {
      for (Question question : set.getQuestions()) {
        assertTrue("Question is not a StackExchangeQuestion", question instanceof StackExchangeQuestion);
        assertEquals("Question does not have right number of correct answers", 1,
            set.getAnswers(question.getId()).size());
      }
    }
  }
}
