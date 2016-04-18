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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;

/**
 * A container class used to represent the features that make up a single vote (upvote/downvote) on
 * a post (stored in a Votes.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_VOTE)
public class Vote implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2635391515666232641L;

  /**
   * The unique ID of this StackExchange vote
   */
  protected Integer id;

  /**
   * The unique ID of the post (question or answer) being voted on
   */
  protected Integer postId;

  /**
   * The type of vote being cast, expressed via the {@link VoteType} enum
   */
  protected VoteType voteType;

  /**
   * The {@link DateTime} that the vote was cast
   */
  protected DateTime creationDate;

  /**
   * The unique user ID for a favorited post (only used when <code>voteType = FAVORITE</code>)
   */
  protected Integer userId;

  /**
   * Bounty amount for a question (only used when <code>voteType = BOUNTY_CLOSE</code>)
   */
  protected Integer bountyAmount;

  /**
   * @return {@link Vote#id}
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ID) public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return {@link Vote#postId}
   */
  public Integer getPostId() {
    return postId;
  }

  /**
   * @param postId the postId to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_POST_ID) public void setPostId(Integer postId) {
    this.postId = postId;
  }

  /**
   * @return {@link Vote#voteType}
   */
  public VoteType getVoteType() {
    return voteType;
  }

  /**
   * @param voteType the voteType to set
   */
  @XmlJavaTypeAdapter(Vote.VoteTypeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_VOTE_TYPE_ID) public void setVoteType(VoteType voteType) {
    this.voteType = voteType;
  }

  /**
   * @return {@link Vote#creationDate}
   */
  public DateTime getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  @XmlJavaTypeAdapter(Vote.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_CREATION_DATE) public void setCreationDate(DateTime creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return {@link Vote#userId}
   */
  public Integer getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_USER_ID) public void setUserId(Integer userId) {
    this.userId = userId;
  }

  /**
   * @return {@link Vote#bountyAmount}
   */
  public Integer getBountyAmount() {
    return bountyAmount;
  }

  /**
   * @param bountyAmount the bountyAmount to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_BOUNTY_AMOUNT) public void setBountyAmount(
      Integer bountyAmount) {
    this.bountyAmount = bountyAmount;
  }

  /**
   * Adapter class for marshaling/unmarshaling the VoteTypeId attribute of StackExchange Votes.xml
   * files
   */
  public static class VoteTypeAdapter extends XmlAdapter<String, VoteType> {

    @Override public String marshal(VoteType type) throws Exception {
      return Integer.toString(type.ordinal() + 1);
    }

    @Override public VoteType unmarshal(String type) throws Exception {
      return VoteType.values()[Integer.valueOf(type) - 1];
    }

  }

  /**
   * Adapter class for marshaling/unmarshaling dates from StackExchange Votes.xml files
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringWriter w = new StringWriter();
    w.append("\n<Vote>\n");
    w.append("\tid: " + id.toString() + "\n");
    w.append("\tpostId: " + postId.toString() + "\n");
    w.append("\tvoteType: " + printIfNotNull(voteType) + "\n");
    w.append("\tcreationDate: " + printIfNotNull(creationDate) + "\n");
    w.append("\tuserId: " + printIfNotNull(userId) + "\n");
    w.append("\tbountyAmount: " + printIfNotNull(bountyAmount));
    return w.toString();
  }

  private String printIfNotNull(Object o) {
    return (o == null) ? "[null]" : o.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override public int hashCode() {
    return 31 + ((id == null) ? 0 : id.hashCode());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Vote other = (Vote) obj;
    if (bountyAmount == null) {
      if (other.bountyAmount != null)
        return false;
    } else if (!bountyAmount.equals(other.bountyAmount))
      return false;
    if (creationDate == null) {
      if (other.creationDate != null)
        return false;
    } else if (!creationDate.equals(other.creationDate))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (postId == null) {
      if (other.postId != null)
        return false;
    } else if (!postId.equals(other.postId))
      return false;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    if (voteType != other.voteType)
      return false;
    return true;
  }
}
