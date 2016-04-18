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

package com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;

/**
 * The class <code>StackExchangeQuestionTest</code> contains tests for the class {@link
 * <code>StackExchangeQuestion</code>}
 * 
 */
public class StackExchangeQuestionTest {

  StackExchangeQuestionTest GIVEN = this, AND = this, WHEN = this, THEN = this;

  private StackExchangeQuestion question = null;

  private StackExchangeThread referenceThread;

  @Test public void test_that_question_fields_match_reference_thread_fields() throws IngestionException {
    GIVEN.reference_thread_is_deserialized();
    WHEN.stack_exchange_question_is_constructed_from_reference_thread();
    THEN.question_fields_match_reference_thread_fields();
  }

  @Test public void test_that_question_title_and_body_can_be_concatenated_and_separated() throws IngestionException {
    GIVEN.reference_thread_is_deserialized();
    WHEN.stack_exchange_question_is_constructed_from_reference_thread();
    THEN.concatenated_question_title_and_body_can_be_separated_correctly();
  }

  private void reference_thread_is_deserialized() throws IngestionException {
    String referenceThreadPath = StackExchangeQuestionTest.class.getResource("/serializedThread/193152.ser").getPath();
    referenceThread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(referenceThreadPath);
  }

  private void stack_exchange_question_is_constructed_from_reference_thread() {
    question = new StackExchangeQuestion(referenceThread);
  }

  private void question_fields_match_reference_thread_fields() {
    assertEquals("Mismatch between reference and question title", referenceThread.getQuestion().getTitle(),
        question.getTitleText());
    assertEquals("Mismatch between reference and question body", referenceThread.getQuestion().getBody(),
        question.getBodyText());
    assertEquals("Incorrect number of question tags", referenceThread.getQuestion().getTags().size(),
        question.getTags().size());
    for (String tag : referenceThread.getQuestion().getTags())
      assertTrue("Question is missing a tag", question.getTags().contains(tag));
    assertEquals("Mismatch between reference and question IDs", Integer.toString(referenceThread.getId()),
        question.getId());
    assertEquals("Invalid engagement string", referenceThread.getSite(), question.getEngagement());
  }

  private void concatenated_question_title_and_body_can_be_separated_correctly() {
    assertEquals("Invalid length for separated text array", 2,
        StackExchangeQuestion.separateQuestionTitleFromBody(question.getText()).length);
    assertEquals("Question title not separated correctly", referenceThread.getQuestion().getTitle(),
        StackExchangeQuestion.separateQuestionTitleFromBody(question.getText())[0]);
    assertEquals("Question body not separated correctly", referenceThread.getQuestion().getBody(),
        StackExchangeQuestion.separateQuestionTitleFromBody(question.getText())[1]);
  }

}
