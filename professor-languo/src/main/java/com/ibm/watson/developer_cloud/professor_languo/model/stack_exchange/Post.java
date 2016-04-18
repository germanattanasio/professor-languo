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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jsoup.Jsoup;

import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.PostType;

/**
 * A container class used to represent the features that make up a single StackExchange post (i.e.,
 * a question or an answer), e.g., title and body texts, author, etc.
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_POST)
public class Post implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 3626105440642660992L;

  /**
   * The unique ID of this StackExchange post
   */
  protected Integer id;

  /**
   * The unique ID of the parent of this post (<code>null</code> if this post is a QUESTION)
   */
  protected Integer parentId;

  /**
   * A {@link PostType} enum that indicates whether this post is a question (
   * <code>postType = QUESTION</code>), an answer ( <code>postType = ANSWER</code>), or other (
   * <code>postType = OTHER</code> )
   */
  protected PostType postType;

  /**
   * The title text of this post
   */
  protected String title;

  /**
   * The body text of this post
   */
  protected String body;

  /**
   * The {@link Post#id id} of the accepted answer to this post, or <code>null</code> if this post
   * is not of postType <code>QUESTION</code>, or if no answer has been accepted
   */
  protected Integer acceptedAnswerId;

  /**
   * The score assigned by StackExchange to this post
   */
  protected Integer score;

  /**
   * The number of page views (<code>null</code> if post is not of postType <code>QUESTION</code>)
   */
  protected Integer viewCount;

  /**
   * The unique ID of the owner of this post
   */
  protected Integer ownerUserId;

  /**
   * The ID of the last editor of this post, or <code>null</code> if post has not been edited
   */
  protected Integer lastEditorUserId;

  /**
   * The date/time of the post's creation
   */
  protected DateTime creationDate;

  /**
   * The date/time the post was closed for additional editing, or <code>null</code> if post is still
   * open
   */
  protected DateTime closedDate;

  /**
   * The date/time of the post's last edit, or <code>null</code> if post has not been edited
   */
  protected DateTime lastEditDate;

  /**
   * The date/time of the last activity on the post
   */
  protected DateTime lastActivityDate;

  /**
   * A list of string tags pertaining to this post, or <code>null</code> if there are no tags
   */
  protected List<String> tags;

  /**
   * The total number of answers, if this post is of postType QUESTION (can be <code>null</code>)
   */
  protected Integer answerCount;

  /**
   * The total number of comments on this post
   */
  protected Integer commentCount;

  /**
   * The number of users who have "favorited" this post, if post is of postType QUESTION (can be
   * <code>null</code>)
   */
  protected Integer favoriteCount;

  /**
   * @return {@link Post#id}
   */
  public Integer getId() {
    return id;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ID) public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return {@link Post#parentId}
   */
  public Integer getParentId() {
    return parentId;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_PARENT_ID) public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  /**
   * @return {@link Post#postType}
   */
  public PostType getPostType() {
    return postType;
  }

  @XmlJavaTypeAdapter(Post.PostTypeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_POST_TYPE_ID) public void setPostTypeId(PostType postType) {
    this.postType = postType;
  }

  /**
   * @return {@link Post#title}
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns title with HTML tags removed
   * 
   * @return {@link Post#title}
   */
  public String getUnformattedTitle() {
    return removeHtmlTags(title);
  }

  @XmlJavaTypeAdapter(Post.HtmlTextAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_TITLE) public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return {@link Post#body}
   */
  public String getBody() {
    return body;
  }

  /**
   * Returns title with HTML tags removed
   * 
   * @return {@link Post#body}
   */
  public String getUnformattedBody() {
    return removeHtmlTags(body);
  }

  @XmlJavaTypeAdapter(Post.HtmlTextAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_BODY) public void setBody(String body) {
    this.body = body;
  }

  /**
   * @return {@link Post#acceptedAnswerId}
   */
  public Integer getAcceptedAnswerId() {
    return acceptedAnswerId;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ACCEPTED_ANSWER_ID) public void setAcceptedAnswerId(
      Integer acceptedAnswerId) {
    this.acceptedAnswerId = acceptedAnswerId;
  }

  /**
   * @return {@link Post#score}
   */
  public Integer getScore() {
    return score;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_SCORE) public void setScore(Integer score) {
    this.score = score;
  }

  /**
   * @return {@link Post#viewCount}
   */
  public Integer getViewCount() {
    return viewCount;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_VIEW_COUNT) public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }

  /**
   * @return {@link Post#ownerUserId}
   */
  public Integer getOwnerUserId() {
    return ownerUserId;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_OWNER_USER_ID) public void setOwnerUserId(Integer ownerUserId) {
    this.ownerUserId = ownerUserId;
  }

  /**
   * @return {@link Post#lastEditorUserId}
   */
  public Integer getLastEditorUserId() {
    return lastEditorUserId;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_LAST_EDITOR_USER_ID) public void setLastEditorUserId(
      Integer lastEditorUserId) {
    this.lastEditorUserId = lastEditorUserId;
  }

  /**
   * @return {@link Post#creationDate}
   */
  public DateTime getCreationDate() {
    return creationDate;
  }

  @XmlJavaTypeAdapter(Post.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_CREATION_DATE) public void setCreationDate(DateTime creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return {@link Post#closedDate}
   */
  public DateTime getClosedDate() {
    return closedDate;
  }

  @XmlJavaTypeAdapter(Post.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_CLOSED_DATE) public void setClosedDate(DateTime closedDate) {
    this.closedDate = closedDate;
  }

  /**
   * @return {@link Post#lastEditDate}
   */
  public DateTime getLastEditDate() {
    return lastEditDate;
  }

  @XmlJavaTypeAdapter(Post.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_LAST_EDIT_DATE) public void setLastEditDate(DateTime lastEditDate) {
    this.lastEditDate = lastEditDate;
  }

  /**
   * @return {@link Post#lastActivityDate}
   */
  public DateTime getLastActivityDate() {
    return lastActivityDate;
  }

  @XmlJavaTypeAdapter(Post.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_LAST_ACTIVITY_DATE) public void setLastActivityDate(
          DateTime lastActivityDate) {
    this.lastActivityDate = lastActivityDate;
  }

  /**
   * @return {@link Post#tags}
   */
  public List<String> getTags() {
    return tags;
  }

  @XmlJavaTypeAdapter(Post.TagsAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_TAGS) public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * @return {@link Post#answerCount}
   */
  public Integer getAnswerCount() {
    return answerCount;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ANSWER_COUNT) public void setAnswerCount(Integer answerCount) {
    this.answerCount = answerCount;
  }

  /**
   * @return {@link Post#commentCount}
   */
  public Integer getCommentCount() {
    return commentCount;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_COMMENT_COUNT) public void setCommentCount(
      Integer commentCount) {
    this.commentCount = commentCount;
  }

  /**
   * @return {@link Post#favoriteCount}
   */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_FAVORITE_COUNT) public void setFavoriteCount(
      Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  /**
   * Adapter class for marshaling/unmarshaling the PostTypeId attribute of StackExchange Posts.xml
   * files
   */
  public static class PostTypeAdapter extends XmlAdapter<String, PostType> {

    @Override public String marshal(PostType type) throws Exception {
      switch (type) {
        case QUESTION:
          return "1";
        case ANSWER:
          return "2";
        default:
          return "3"; // Catch-all for "OTHER"
      }
    }

    @Override public PostType unmarshal(String type) throws Exception {
      if (type.equals("1"))
        return PostType.QUESTION;
      else if (type.equals("2"))
        return PostType.ANSWER;
      else
        return PostType.OTHER;
    }

  }

  /**
   * Adapter class for marshaling/unmarshaling dates from StackExchange Posts.xml files
   */
  public static class DateTimeAdapter extends XmlAdapter<String, DateTime> {

    DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

    @Override public String marshal(DateTime date) throws Exception {
      return date.toString(fmt);
    }

    @Override public DateTime unmarshal(String date) throws Exception {
      return fmt.parseDateTime(date);
    }

  }

  /**
   * Adapter class for marshaling/unmarshaling a list of tags from StackExchange Posts.xml files
   */
  public static class TagsAdapter extends XmlAdapter<String, List<String>> {

    @Override public String marshal(List<String> tags) throws Exception {
      StringWriter writer = new StringWriter();
      for (String tag : tags) {
        writer.append(StringEscapeUtils.escapeHtml4("<" + tag + ">"));
      }
      return writer.toString();
    }

    @Override public List<String> unmarshal(String tags) throws Exception {
      String unescapedHtmlTags = StringEscapeUtils.unescapeHtml4(tags);
      return Arrays.asList(unescapedHtmlTags.replaceAll("<", "").split(">"));
    }

  }

  /**
   * Adapter class for marshaling/unmarshaling text containing HTML markup from StackExchange
   * Posts.xml files
   */
  public static class HtmlTextAdapter extends XmlAdapter<String, String> {

    @Override public String marshal(String htmlTextOut) throws Exception {
      return StringEscapeUtils.escapeHtml4(htmlTextOut);
    }

    @Override public String unmarshal(String htmlTextIn) throws Exception {
      return StringEscapeUtils.unescapeHtml4(htmlTextIn);
    }

  }

  private String removeHtmlTags(String in) {
    if (in != null) {
      return Jsoup.parse(in).text();
    } else {
      return null;
    }
  }

  @Override public String toString() {
    StringWriter w = new StringWriter();
    w.append("\n<Post>\n");
    w.append("\t{id: " + id.toString() + "\n");
    w.append("\tpostType: " + printIfNotNull(postType) + "\n");
    w.append("\tparentId: " + printIfNotNull(parentId) + "\n");
    w.append("\ttitle: " + printIfNotNull(title) + "\n");
    w.append("\tbody: " + printIfNotNull(body) + "\n");
    w.append("\ttags: " + printIfNotNull(tags) + "\n");
    w.append("\tacceptedAnswerId: " + printIfNotNull(acceptedAnswerId) + "\n");
    w.append("\tscore: " + printIfNotNull(score) + "\n");
    w.append("\tviewCount: " + printIfNotNull(viewCount) + "\n");
    w.append("\townerUserId: " + printIfNotNull(ownerUserId) + "\n");
    w.append("\tlastEditorUserId: " + printIfNotNull(lastEditorUserId) + "\n");
    w.append("\tcreationDate: " + printIfNotNull(creationDate) + "\n");
    w.append("\tclosedDate: " + printIfNotNull(closedDate) + "\n");
    w.append("\tlastEditDate: " + printIfNotNull(lastEditDate) + "\n");
    w.append("\tlastActivityDate: " + printIfNotNull(lastActivityDate) + "\n");
    w.append("\tanswerCount: " + printIfNotNull(answerCount) + "\n");
    w.append("\tcommentCount: " + printIfNotNull(commentCount) + "\n");
    w.append("\tfavoriteCount: " + printIfNotNull(favoriteCount));
    w.append("\n\t}\n");
    return w.toString();
  }

  private String printIfNotNull(Object o) {
    return (o == null) ? "[null]" : o.toString();
  }

  @Override public int hashCode() {
    return 31 + ((id == null) ? 0 : id.hashCode());
  }

  @Override public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Post other = (Post) obj;
    if (acceptedAnswerId == null) {
      if (other.acceptedAnswerId != null)
        return false;
    } else if (!acceptedAnswerId.equals(other.acceptedAnswerId))
      return false;
    if (answerCount == null) {
      if (other.answerCount != null)
        return false;
    } else if (!answerCount.equals(other.answerCount))
      return false;
    if (body == null) {
      if (other.body != null)
        return false;
    } else if (!body.equals(other.body))
      return false;
    if (closedDate == null) {
      if (other.closedDate != null)
        return false;
    } else if (!closedDate.equals(other.closedDate))
      return false;
    if (commentCount == null) {
      if (other.commentCount != null)
        return false;
    } else if (!commentCount.equals(other.commentCount))
      return false;
    if (creationDate == null) {
      if (other.creationDate != null)
        return false;
    } else if (!creationDate.equals(other.creationDate))
      return false;
    if (favoriteCount == null) {
      if (other.favoriteCount != null)
        return false;
    } else if (!favoriteCount.equals(other.favoriteCount))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (lastActivityDate == null) {
      if (other.lastActivityDate != null)
        return false;
    } else if (!lastActivityDate.equals(other.lastActivityDate))
      return false;
    if (lastEditDate == null) {
      if (other.lastEditDate != null)
        return false;
    } else if (!lastEditDate.equals(other.lastEditDate))
      return false;
    if (lastEditorUserId == null) {
      if (other.lastEditorUserId != null)
        return false;
    } else if (!lastEditorUserId.equals(other.lastEditorUserId))
      return false;
    if (ownerUserId == null) {
      if (other.ownerUserId != null)
        return false;
    } else if (!ownerUserId.equals(other.ownerUserId))
      return false;
    if (parentId == null) {
      if (other.parentId != null)
        return false;
    } else if (!parentId.equals(other.parentId))
      return false;
    if (postType != other.postType)
      return false;
    if (score == null) {
      if (other.score != null)
        return false;
    } else if (!score.equals(other.score))
      return false;
    if (tags == null) {
      if (other.tags != null)
        return false;
    } else if (!tags.equals(other.tags))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (viewCount == null) {
      if (other.viewCount != null)
        return false;
    } else if (!viewCount.equals(other.viewCount))
      return false;
    return true;
  }
}
