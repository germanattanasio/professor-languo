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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;

/**
 * This container class represents a single answer on a Stack Exchange forum, and contains, in
 * addition to the underlying {@link Post}, information about the {@link User} who authored the
 * post, and any {@link Vote Votes} or {@link PostLink PostLinks} associated with the answer.
 *
 */
public class StackExchangeAnswer implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4400776610848881733L;

  /**
   * The particular StackExchange site corresponding to this answer (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * The {@link Post} corresponding to this answer
   */
  protected Post answer;

  /**
   * The {@link User} who authored this answer
   */
  protected User author;

  /**
   * A map from a {@link VoteType} to a Set of {@link Vote} objects that were cast for this answer
   */
  protected Map<VoteType, Set<Vote>> voteMap;

  /**
   * A list of {@link PostLink} links attached to this answer
   */
  protected List<PostLink> postLinkList;

  /**
   * Create a new {@link StackExchangeAnswer}
   * 
   * @param answer - The {@link Post} representing this answer
   * @param author - The {@link User} who authored this answer
   * @param site - The URL of the StackExchange site hosting this answer (e.g., "stackoverflow.com")
   */
  public StackExchangeAnswer(Post answer, User author, String site) {
    this(answer, author, site, null, null);
  }

  /**
   * Create a new {@link StackExchangeAnswer}
   * 
   * @param answer - The {@link Post} representing this answer
   * @param author - The {@link User} who authored this answer
   * @param site - The URL of the StackExchange site hosting this answer (e.g., "stackoverflow.com")
   * @param votes - A collection of {@link Vote Votes} associated with this answer
   * @param links - A collection of {@link PostLink PostLinks} associated with this answer
   */
  public StackExchangeAnswer(Post answer, User author, String site, Set<Vote> votes, Set<PostLink> links) {
    super();
    this.answer = answer;
    this.author = author;
    this.site = site;
    this.voteMap = new HashMap<StackExchangeConstants.VoteType, Set<Vote>>(20);
    if (votes != null)
      addVotesToMap(votes);
    this.postLinkList = new ArrayList<PostLink>();
    if (links != null)
      addPostLinks(links);
  }

  /**
   * @return The particular StackExchange site corresponding to this answer (e.g.,
   *         "stackoverflow.com")
   */
  public String getSite() {
    return site;
  }

  /**
   * @return The {@link Post} corresponding to this answer
   */
  public Post getAnswer() {
    return answer;
  }

  /**
   * @return The {@link User} who authored this answer
   */
  public User getAuthor() {
    return author;
  }

  /**
   * @return A map from a {@link VoteType} to a Set of {@link Vote} objects that were cast for this
   *         answer
   */
  public Map<VoteType, Set<Vote>> getVoteMap() {
    return voteMap;
  }

  /**
   * @return A list of {@link PostLink} links attached to this answer
   */
  public List<PostLink> getPostLinkList() {
    return postLinkList;
  }

  /**
   * @return id - The unique ID of this answer, i.e., {@link Post#id}
   */
  public int getId() {
    return answer.getId();
  }

  /**
   * Add a new {@link Vote} to this answer
   * 
   * @param vote - The Vote to add
   */
  public void addVoteToMap(Vote vote) {
    if (voteMap.containsKey(vote.getVoteType())) {
      // Add this vote to an existing set
      voteMap.get(vote.getVoteType()).add(vote);
    } else {
      Set<Vote> voteSet = new HashSet<Vote>(8);
      voteSet.add(vote);
      voteMap.put(vote.getVoteType(), voteSet);
    }
  }

  /**
   * Add a collection of {@link Vote} objects to this answer
   * 
   * @param votes - The collection of {@link Vote} objects to add
   */
  public void addVotesToMap(Collection<Vote> votes) {
    for (Vote v : votes) {
      addVoteToMap(v);
    }
  }

  /**
   * Add a new {@link PostLink} to this answer
   *
   * @param link - The PostLink to add
   */
  public void addPostLink(PostLink link) {
    postLinkList.add(link);
  }

  /**
   * Add a collection of {@link PostLink} objects to this answer
   *
   * @param links - The collection of {@link PostLink} objects to add
   */
  public void addPostLinks(Collection<PostLink> links) {
    for (PostLink p : links) {
      addPostLink(p);
    }
  }

  /**
   * @param voteType - The {@link VoteType} whose tally you would like
   * @return The tally of votes of the specified type cast for this answer
   */
  public int getVoteCount(VoteType voteType) {
    if (voteMap.containsKey(voteType))
      return voteMap.get(voteType).size();
    else
      return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override public String toString() {
    return "\n<StackExchangeAnswer>\n" + "\t{SITE: " + site + "\n" + "\tANSWER: " + indentString(printIfNotNull(answer))
        + "\tAUTHOR: " + indentString(printIfNotNull(author)) + "\tVOTE MAP: " + indentString(printIfNotNull(voteMap))
        + "\tPOSTLINK LIST: " + indentString(printIfNotNull(postLinkList)) + "\n\t}\n";
  }

  private String indentString(String inputString) {
    String[] splitString = inputString.split("\n");
    StringBuilder sb = new StringBuilder(inputString.length() + splitString.length);
    for (String line : splitString) {
      sb.append("\t").append(line).append("\n");
    }
    return sb.toString();
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
    return getId();
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
    StackExchangeAnswer other = (StackExchangeAnswer) obj;
    if (answer == null) {
      if (other.answer != null)
        return false;
    } else if (!answer.equals(other.answer))
      return false;
    if (author == null) {
      if (other.author != null)
        return false;
    } else if (!author.equals(other.author))
      return false;
    if (postLinkList == null) {
      if (other.postLinkList != null)
        return false;
    } else if (!postLinkList.equals(other.postLinkList))
      return false;
    if (site == null) {
      if (other.site != null)
        return false;
    } else if (!site.equals(other.site))
      return false;
    if (voteMap == null) {
      if (other.voteMap != null)
        return false;
    } else if (!voteMap.equals(other.voteMap))
      return false;
    return true;
  }
}
