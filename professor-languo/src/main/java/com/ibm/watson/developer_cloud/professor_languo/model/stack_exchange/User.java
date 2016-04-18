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

import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * A container class used to represent the features that make up a single StackExchange user, e.g.,
 * user ID, reputation, etc.
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_USER)
public class User implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8041318086806117546L;

  /**
   * The unique ID of this StackExchange user
   */
  protected Integer id;

  /**
   * The number of reputation points held by the user
   */
  protected Integer reputation;

  /**
   * The user's display name on the StackExchange sites
   */
  protected String displayName;

  /**
   * A hash of the user's registered email address
   */
  protected String emailHash;

  /**
   * The user's "about me" blurb
   */
  protected String aboutMe;

  /**
   * The date/time of the post's creation
   */
  protected DateTime creationDate;

  /**
   * The date/time of the last activity on the site
   */
  protected DateTime lastAccessDate;

  /**
   * Optional URL for user's personal page
   */
  protected String websiteUrl;

  /**
   * User's physical location
   */
  protected String location;

  /**
   * User's age (in years)
   */
  protected Integer age;

  /**
   * Cumulative number of up-votes received by user across all posts
   */
  protected Integer upVotes;

  /**
   * Cumulative number of down-votes received by user across all posts
   */
  protected Integer downVotes;

  /**
   * Cumulative number of views received by user's posts
   */
  protected Integer views;

  /**
   * User's StackExchange account ID (you should probably be referencing {@link User#id} though)
   */
  protected Integer accountId;

  /**
   * @return {@link User#id}
   */
  public int getId() {
    return id;
  }

  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ID) public void setId(int id) {
    this.id = id;
  }

  /**
   * @return {@link User#reputation}
   */
  public Integer getReputation() {
    return reputation;
  }

  /**
   * @param reputation the reputation to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_REPUTATION) public void setReputation(Integer reputation) {
    this.reputation = reputation;
  }

  /**
   * @return {@link User#displayName}
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName the displayName to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_DISPLAY_NAME) public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return {@link User#emailHash}
   */
  public String getEmailHash() {
    return emailHash;
  }

  /**
   * @param emailHash the emailHash to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_EMAIL_HASH) public void setEmailHash(String emailHash) {
    this.emailHash = emailHash;
  }

  /**
   * @return {@link User#websiteUrl}
   */
  public String getWebsiteUrl() {
    return websiteUrl;
  }

  /**
   * @param websiteUrl the websiteUrl to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_WEBSITE_URL) public void setWebsiteUrl(String websiteUrl) {
    this.websiteUrl = websiteUrl;
  }

  /**
   * @return {@link User#location}
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_LOCATION) public void setLocation(String location) {
    this.location = location;
  }

  /**
   * @return {@link User#age}
   */
  public Integer getAge() {
    return age;
  }

  /**
   * @param age the age to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_AGE) public void setAge(Integer age) {
    this.age = age;
  }

  /**
   * @return {@link User#upVotes}
   */
  public Integer getUpVotes() {
    return upVotes;
  }

  /**
   * @param upVotes the upVotes to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_UP_VOTES) public void setUpVotes(Integer upVotes) {
    this.upVotes = upVotes;
  }

  /**
   * @return {@link User#downVotes}
   */
  public Integer getDownVotes() {
    return downVotes;
  }

  /**
   * @param downVotes the downVotes to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_DOWN_VOTES) public void setDownVotes(Integer downVotes) {
    this.downVotes = downVotes;
  }

  /**
   * @return {@link User#views}
   */
  public Integer getViews() {
    return views;
  }

  /**
   * @param views the views to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_VIEWS) public void setViews(Integer views) {
    this.views = views;
  }

  /**
   * @return {@link User#accountId}
   */
  public Integer getAccountId() {
    return accountId;
  }

  /**
   * @param accountId the accountId to set
   */
  @XmlAttribute(name = StackExchangeConstants.XML_ATTR_ACCOUNT_ID) public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  /**
   * @return {@link User#aboutMe}
   */
  public String getAboutMe() {
    return aboutMe;
  }

  @XmlJavaTypeAdapter(User.HtmlTextAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_ABOUT_ME) public void setAboutMe(String blurb) {
    this.aboutMe = blurb;
  }

  /**
   * @return {@link User#creationDate}
   */
  public DateTime getCreationDate() {
    return creationDate;
  }

  @XmlJavaTypeAdapter(User.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_CREATION_DATE) public void setCreationDate(DateTime creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return {@link User#lastAccessDate}
   */
  public DateTime getLastAccessDate() {
    return lastAccessDate;
  }

  @XmlJavaTypeAdapter(User.DateTimeAdapter.class) @XmlAttribute(
      name = StackExchangeConstants.XML_ATTR_LAST_ACCESS_DATE) public void setLastAccessDate(DateTime lastAccessDate) {
    this.lastAccessDate = lastAccessDate;
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

  @Override public String toString() {
    StringWriter w = new StringWriter();
    w.append("\n<User>\n");
    w.append("\t{id: " + id.toString() + "\n");
    w.append("\treputation: " + printIfNotNull(reputation) + "\n");
    w.append("\tdisplayName: " + printIfNotNull(displayName) + "\n");
    w.append("\temailHash: " + printIfNotNull(emailHash) + "\n");
    w.append("\tcreationDate: " + printIfNotNull(creationDate) + "\n");
    w.append("\tlastAccessDate: " + printIfNotNull(lastAccessDate) + "\n");
    w.append("\twebsiteUrl: " + printIfNotNull(websiteUrl) + "\n");
    w.append("\tlocation: " + printIfNotNull(location) + "\n");
    w.append("\tage: " + printIfNotNull(age) + "\n");
    w.append("\tupVotes: " + printIfNotNull(upVotes) + "\n");
    w.append("\tdownVotes: " + printIfNotNull(downVotes) + "\n");
    w.append("\taccountId: " + printIfNotNull(accountId) + "\n");
    w.append("\taboutMe: " + printIfNotNull(aboutMe));
    w.append("\n\t}\n");

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
    User other = (User) obj;
    if (aboutMe == null) {
      if (other.aboutMe != null)
        return false;
    } else if (!aboutMe.equals(other.aboutMe))
      return false;
    if (accountId == null) {
      if (other.accountId != null)
        return false;
    } else if (!accountId.equals(other.accountId))
      return false;
    if (age == null) {
      if (other.age != null)
        return false;
    } else if (!age.equals(other.age))
      return false;
    if (creationDate == null) {
      if (other.creationDate != null)
        return false;
    } else if (!creationDate.equals(other.creationDate))
      return false;
    if (displayName == null) {
      if (other.displayName != null)
        return false;
    } else if (!displayName.equals(other.displayName))
      return false;
    if (downVotes == null) {
      if (other.downVotes != null)
        return false;
    } else if (!downVotes.equals(other.downVotes))
      return false;
    if (emailHash == null) {
      if (other.emailHash != null)
        return false;
    } else if (!emailHash.equals(other.emailHash))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (lastAccessDate == null) {
      if (other.lastAccessDate != null)
        return false;
    } else if (!lastAccessDate.equals(other.lastAccessDate))
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    if (reputation == null) {
      if (other.reputation != null)
        return false;
    } else if (!reputation.equals(other.reputation))
      return false;
    if (upVotes == null) {
      if (other.upVotes != null)
        return false;
    } else if (!upVotes.equals(other.upVotes))
      return false;
    if (views == null) {
      if (other.views != null)
        return false;
    } else if (!views.equals(other.views))
      return false;
    if (websiteUrl == null) {
      if (other.websiteUrl != null)
        return false;
    } else if (!websiteUrl.equals(other.websiteUrl))
      return false;
    return true;
  }
}
