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

import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.LinkType;

/**
 * A container class used to represent the features that make up a single post link (a link that
 * relates two distinct StackExchange posts) (stored in a PostLinks.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_POST_LINK)
public class PostLink implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1256172754320724506L;

  /**
   * The unique ID of this StackExchange post link
   */
  protected Integer id;

  /**
   * The unique ID of the post containing the link
   */
  protected Integer postId;

  /**
   * The unique ID of the post to which {@link PostLink#postId} is being linked
   */
  protected Integer relatedPostId;

  /**
   * The type of post link, expressed via the {@link LinkType} enum
   */
  protected LinkType linkType;

  /**
   * The {@link DateTime} that the vote was cast
   */
  protected DateTime creationDate;

  /**
   * @return {@link PostLink#id}
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
   * @return {@link PostLink#postId}
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
   * @return {@link PostLink#relatedPostId}
   */
  public Integer getRelatedPostId() {
    return relatedPostId;
  }

  /**
   * @param relatedPostId the relatedPostId to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_RELATED_POST_ID) public void setRelatedPostId(
      Integer relatedPostId) {
    this.relatedPostId = relatedPostId;
  }

  /**
   * @return {@link PostLink#linkType}
   */
  public LinkType getLinkType() {
    return linkType;
  }

  /**
   * @param linkType the linkType to set
   */
  @XmlJavaTypeAdapter(PostLink.LinkTypeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_LINK_TYPE_ID) public void setLinkType(LinkType linkType) {
    this.linkType = linkType;
  }

  /**
   * @return {@link PostLink#creationDate}
   */
  public DateTime getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  @XmlJavaTypeAdapter(PostLink.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_CREATION_DATE) public void setCreationDate(DateTime creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Adapter class for marshaling/unmarshaling the LinkTypeId attribute of StackExchange
   * PostLinks.xml files
   */
  public static class LinkTypeAdapter extends XmlAdapter<String, LinkType> {

    @Override public String marshal(LinkType type) throws Exception {
      return Integer.toString(type.ordinal() + 1);
    }

    @Override public LinkType unmarshal(String type) throws Exception {
      return LinkType.values()[Integer.valueOf(type) - 1];
    }

  }

  /**
   * Adapter class for marshaling/unmarshaling dates from StackExchange PostLinks.xml files
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
    w.append("\n<PostLink>\n");
    w.append("\tid: " + id.toString() + "\n");
    w.append("\tpostId: " + postId.toString() + "\n");
    w.append("\trelatedPostId: " + relatedPostId.toString() + "\n");
    w.append("\tlinkType: " + printIfNotNull(linkType) + "\n");
    w.append("\tcreationDate: " + printIfNotNull(creationDate));
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
    PostLink other = (PostLink) obj;
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
    if (linkType != other.linkType)
      return false;
    if (relatedPostId == null) {
      if (other.relatedPostId != null)
        return false;
    } else if (!relatedPostId.equals(other.relatedPostId))
      return false;
    return true;
  }
}
