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

import java.util.ArrayList;
import java.util.List;

import com.ibm.watson.developer_cloud.professor_languo.data_model.Question;
import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;

/**
 * A StackExchangeQuestion extends a pipeline {@link Question}, and represents a question that was
 * asked of a Stack Exchange site.
 *
 */
public class StackExchangeQuestion extends Question {

  private static final long serialVersionUID = 5625147879532563069L;

  private final TextWithAnalysis title, body;

  private final List<String> tags;

  public static final String QUESTION_TITLE_AND_BODY_SEPARATOR = " |||| ";
  public static final String QUESTION_TITLE_AND_BODY_SEPARATOR_REGEX = "\\s\\|\\|\\|\\|\\s";

  /**
   * Create a new {@link StackExchangeQuestion} from a given {@link StackExchangeThread}. The
   * {@link Question} text consists of a concatenation of the Stack Exchange question post's title
   * and body, the question engagement and source are given by the Stack Exchange site, and the
   * question ID is given by the Stack Exchange thread ID
   * 
   * @param stackExchangeThread - The {@link StackExchangeThread} from which a {@link Question} is
   *        to be extracted
   */
  public StackExchangeQuestion(StackExchangeThread stackExchangeThread) {
    super(
        concatenateQuestionTitleAndBody(stackExchangeThread.getQuestion().getTitle(),
            stackExchangeThread.getQuestion().getBody()),
        stackExchangeThread.getSite(), Integer.toString(stackExchangeThread.getId()), stackExchangeThread.getSite());

    this.title = new TextWithAnalysis(stackExchangeThread.getQuestion().getTitle());
    this.body = new TextWithAnalysis(stackExchangeThread.getQuestion().getBody());
    this.tags = stackExchangeThread.getQuestion().getTags();
  }

  /**
   * Create a new {@link StackExchangeQuestion} with the provided fields
   * 
   * @param title - The question post's title text
   * @param body - The question post's body text
   * @param tags - A list of tags associated with the question post
   * @param id - A unique ID for this question
   * @param site - The Stack Exchange site for this question
   */
  public StackExchangeQuestion(String title, String body, List<String> tags, Integer id, String site) {
    super(concatenateQuestionTitleAndBody(title, body), site, Integer.toString(id), site);

    this.title = new TextWithAnalysis(title);
    this.body = new TextWithAnalysis(body);
    this.tags = (tags == null) ? new ArrayList<String>(4) : tags;
  }

  /**
   * @return {@link TextWithAnalysis} for the Stack Exchange question's title
   */
  public TextWithAnalysis getTitle() {
    return title;
  }

  /**
   * @return The Stack Exchange question's title text
   */
  public String getTitleText() {
    return title.getText();
  }

  /**
   * @return {@link TextWithAnalysis} for the Stack Exchange question's body
   */
  public TextWithAnalysis getBody() {
    return body;
  }

  /**
   * @return The Stack Exchange question's body text
   */
  public String getBodyText() {
    return body.getText();
  }

  /**
   * @return A list of tags associated with this Stack Exchange question
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * @param concatenatedTitleAndBody - The concatenated text that is retrieved from
   *        {@link StackExchangeQuestion#getText()}
   * 
   * @return A 2-element string array, with the first element corresponding to the question's title
   *         text, and the second element corresponding to the question's body text
   */
  public static String[] separateQuestionTitleFromBody(String concatenatedTitleAndBody) {
    return concatenatedTitleAndBody.split(QUESTION_TITLE_AND_BODY_SEPARATOR_REGEX);
  }

  /**
   * @param titleText - The question title text
   * @param bodyText - The question body text
   * 
   * @return A concatenation of the two texts, separated by
   *         {@link StackExchangeQuestion#QUESTION_TITLE_AND_BODY_SEPARATOR}
   */
  private static String concatenateQuestionTitleAndBody(String titleText, String bodyText) {
    return ((titleText == null) ? "" : titleText) + QUESTION_TITLE_AND_BODY_SEPARATOR
        + ((bodyText == null) ? "" : bodyText);
  }

}
