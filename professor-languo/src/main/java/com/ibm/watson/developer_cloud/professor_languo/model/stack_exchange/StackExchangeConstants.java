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

import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.IndexingStats;

/**
 * String constants specific to the StackExchange corpora
 *
 */
public class StackExchangeConstants {

  // Constants used in the XML schema of StackExchange Internet Archive data
  // dumps
  // -- Posts.xml
  public static final String XML_ELEM_POSTS = "posts";
  public static final String XML_ELEM_POST = "row";
  public static final String XML_ATTR_ID = "Id";
  public static final String XML_ATTR_PARENT_ID = "ParentId";
  public static final String XML_ATTR_POST_TYPE_ID = "PostTypeId";
  public static final String XML_ATTR_ACCEPTED_ANSWER_ID = "AcceptedAnswerId";
  public static final String XML_ATTR_CREATION_DATE = "CreationDate";
  public static final String XML_ATTR_SCORE = "Score";
  public static final String XML_ATTR_VIEW_COUNT = "ViewCount";
  public static final String XML_ATTR_BODY = "Body";
  public static final String XML_ATTR_OWNER_USER_ID = "OwnerUserId";
  public static final String XML_ATTR_LAST_EDITOR_USER_ID = "LastEditorUserId";
  public static final String XML_ATTR_LAST_EDIT_DATE = "LastEditDate";
  public static final String XML_ATTR_LAST_ACTIVITY_DATE = "LastActivityDate";
  public static final String XML_ATTR_CLOSED_DATE = "ClosedDate";
  public static final String XML_ATTR_TITLE = "Title";
  public static final String XML_ATTR_TAGS = "Tags";
  public static final String XML_ATTR_ANSWER_COUNT = "AnswerCount";
  public static final String XML_ATTR_COMMENT_COUNT = "CommentCount";
  public static final String XML_ATTR_FAVORITE_COUNT = "FavoriteCount";
  // -- Votes.xml
  public static final String XML_ELEM_VOTES = "votes";
  public static final String XML_ELEM_VOTE = "row";
  public static final String XML_ATTR_POST_ID = "PostId";
  public static final String XML_ATTR_VOTE_TYPE_ID = "VoteTypeId";
  public static final String XML_ATTR_USER_ID = "UserId";
  public static final String XML_ATTR_BOUNTY_AMOUNT = "BountyAmount";
  // -- Users.xml
  public static final String XML_ELEM_USERS = "users";
  public static final String XML_ELEM_USER = "row";
  public static final String XML_ATTR_REPUTATION = "Reputation";
  public static final String XML_ATTR_DISPLAY_NAME = "DisplayName";
  public static final String XML_ATTR_LAST_ACCESS_DATE = "LastAccessDate";
  public static final String XML_ATTR_EMAIL_HASH = "EmailHash";
  public static final String XML_ATTR_WEBSITE_URL = "WebsiteUrl";
  public static final String XML_ATTR_LOCATION = "Location";
  public static final String XML_ATTR_AGE = "Age";
  public static final String XML_ATTR_ABOUT_ME = "AboutMe";
  public static final String XML_ATTR_VIEWS = "Views";
  public static final String XML_ATTR_UP_VOTES = "UpVotes";
  public static final String XML_ATTR_DOWN_VOTES = "DownVotes";
  public static final String XML_ATTR_ACCOUNT_ID = "AccountId";
  // -- PostLinks.xml
  public static final String XML_ELEM_POST_LINKS = "postlinks";
  public static final String XML_ELEM_POST_LINK = "row";
  public static final String XML_ATTR_RELATED_POST_ID = "RelatedPostId";
  public static final String XML_ATTR_LINK_TYPE_ID = "LinkTypeId";
  // -- misc (file paths ...)

  public static final String DUP_THREAD_TSV_FILE_NAME = "dup_thread";
  public static final String DUP_THREAD_TSV_FILE_EXTENSION = ".tsv";
  public static final String DUP_THREAD_TSV_TRAIN_FILE_SUFFIX = "_train";
  public static final String DUP_THREAD_TSV_TEST_FILE_SUFFIX = "_test";
  public static final String DUP_THREAD_TSV_VALIDATE_FILE_SUFFIX = "_validate";

  public static final String BIN_FILE_SUFFIX = ".ser";
  public static final String QUERY_STRING = "*:*";

  /**
   * An enum that is used to denote a post as containing a <code>QUESTION</code> or an
   * <code>ANSWER</code>
   */
  public enum PostType {
    QUESTION, ANSWER, OTHER
  };

  /**
   * An enum that is used to denote the type of vote being cast
   */
  public enum VoteType {
    ACCEPTED_BY_ORIGINATOR, UP_MOD, DOWN_MOD, OFFENSIVE, FAVORITE, CLOSE, REOPEN, BOUNTY_START, BOUNTY_CLOSE, DELETION, UNDELETION, SPAM, INFORM_MODERATOR
  };

  /**
   * An enum that is used to denote the nature of a post link (i.e., <code>LINKED</code> or
   * <code>DUPLICATE</code>)
   */
  public enum LinkType {
    LINKED, INVALID, DUPLICATE
  };

  /**
   * An enum that is used to indicate the field names that the Lucene searcher can search
   */
  public enum IndexDocumentFieldName {
    THREAD_POST_ID, THREAD_TITLE, THREAD_TEXT, THREAD_TAGS, ACCEPTED_ANSWER_TEXT, TOP_VOTED_ANSWER_TEXT, CONCATENATED_ANSWERS_TEXT, SERIALIZED_THREAD
  };

  /**
   * An enum used to indicate the type of the statistics in the {@link IndexingStats}.
   */
  public enum IndexStatFieldName {
    INDEX_DOC_NUM, INDEX_TIME, INDEX_THREAD_IDS, INDEX_DOCS_FIELD_TYPE, INDEX_DOCS_FIELD_NAME
  };

  /**
   * An enum used to indicate the type of information stored in the txt file representing the
   * duplicate StackExchangeThread
   */
  public enum DupThreadTxtFieldName {
    THREAD_POST_ID, THREAD_TITLE, THREAD_TEXT, ORIGINAL_POST_ID, THREAD_BIN_FILE_NAME
  };

}
