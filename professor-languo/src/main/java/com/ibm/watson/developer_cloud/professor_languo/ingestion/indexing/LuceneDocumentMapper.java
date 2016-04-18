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

package com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;

public class LuceneDocumentMapper implements DocumentMapper {
  @Override public void initialize(Properties properties) throws IngestionException {

  }

  @Override public Document createDocument(StackExchangeThread question) throws IngestionException {
    Document doc = new Document();
    doc.add(new IntField(IndexDocumentFieldName.THREAD_POST_ID.toString(), question.getId(), Field.Store.YES));

    String questionTitle = question.getQuestion().getUnformattedTitle();
    doc.add(new TextField(IndexDocumentFieldName.THREAD_TITLE.toString(), (questionTitle == null) ? "" : questionTitle,
        Field.Store.YES));

    String questionBody = question.getQuestion().getUnformattedBody();
    doc.add(new TextField(IndexDocumentFieldName.THREAD_TEXT.toString(), (questionBody == null) ? "" : questionBody,
        Field.Store.YES));

    List<String> tags = question.getQuestion().getTags();
    doc.add(new TextField(IndexDocumentFieldName.THREAD_TAGS.toString(), (tags == null) ? "" : tags.toString(),
        Field.Store.YES));

    doc.add(new TextField(IndexDocumentFieldName.ACCEPTED_ANSWER_TEXT.toString(), question.getAcceptedAnswerText(),
        Field.Store.YES));

    doc.add(new TextField(IndexDocumentFieldName.TOP_VOTED_ANSWER_TEXT.toString(), question.getTopVotedAnswerText(),
        Field.Store.YES));

    doc.add(new TextField(IndexDocumentFieldName.CONCATENATED_ANSWERS_TEXT.toString(),
        question.getConcatenatedAnswersText(), Field.Store.YES));

    byte[] serializedThread = StackExchangeThreadSerializer.serializeObjToBinArr(question);
    doc.add(new StoredField(IndexDocumentFieldName.SERIALIZED_THREAD.toString(), serializedThread));

    return doc;
  }

  @Override public List<FieldType> getFieldTypes() {
    return Arrays.asList(IntField.TYPE_STORED, TextField.TYPE_STORED, TextField.TYPE_STORED, TextField.TYPE_STORED,
        TextField.TYPE_STORED, TextField.TYPE_STORED, TextField.TYPE_NOT_STORED, BinaryDocValuesField.TYPE);
  }

  @Override public Set<String> getFieldNames() {
    return new HashSet<String>(Arrays.asList(IndexDocumentFieldName.THREAD_POST_ID.toString(),
        IndexDocumentFieldName.THREAD_TITLE.toString(), IndexDocumentFieldName.THREAD_TEXT.toString(),
        IndexDocumentFieldName.THREAD_TAGS.toString(), IndexDocumentFieldName.ACCEPTED_ANSWER_TEXT.toString(),
        IndexDocumentFieldName.TOP_VOTED_ANSWER_TEXT.toString(),
        IndexDocumentFieldName.CONCATENATED_ANSWERS_TEXT.toString(),
        IndexDocumentFieldName.SERIALIZED_THREAD.toString()));
  }
}
