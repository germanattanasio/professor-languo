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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This container class is used to store a list of StackExchange votes (e.g., up-votes and
 * down-votes), (stored in a Votes.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_VOTES)
public class Votes {

  /**
   * The particular StackExchange site corresponding to these votes (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * A List of individual StackExchange {@link Vote} objects
   */
  protected List<Vote> votes;

  private Map<Integer, Set<Vote>> postIdToVotesMap;

  /**
   * @return {@link Votes#site site}
   */
  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  /**
   * @return {@link Votes#votes votes}
   */
  public List<Vote> getVotes() {
    return votes;
  }

  @XmlElement(name = StackExchangeConstants.XML_ELEM_VOTE) public void setVotes(List<Vote> votes) {
    this.votes = votes;
  }

  /**
   * @param postId - The {@link Post#id ID} of the {@link Post} whose votes are to be retrieved
   * @return A Set of {@link Vote} objects, or <code>null</code> if no votes were cast for this post
   *         ID
   */
  public Set<Vote> getVotesByPostId(Integer postId) {
    // Start by checking if we've populated a map of <post ID, votes> pairs.
    // If not, populate that map first
    if (postIdToVotesMap == null) {
      populateMap();
    }
    return (postIdToVotesMap == null) ? null : postIdToVotesMap.get(postId);
  }

  /**
   * Populate a map of <post ID,votes> pairs
   */
  private void populateMap() {
    if (votes == null)
      return; // There's nothing to do yet
    else
      postIdToVotesMap = new HashMap<Integer, Set<Vote>>(votes.size());

    for (Vote vote : votes) {
      if (postIdToVotesMap.containsKey(vote.getPostId()))
        postIdToVotesMap.get(vote.getPostId()).add(vote);
      else {
        HashSet<Vote> voteSet = new HashSet<Vote>(8);
        voteSet.add(vote);
        postIdToVotesMap.put(vote.getPostId(), voteSet);
      }
    }
  }

}
