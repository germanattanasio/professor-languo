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
 * This container class is used to store a list of StackExchange post links (i.e., linked and
 * duplicate posts) (stored in a PostLinks.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_POST_LINKS)
public class PostLinks {

  /**
   * The particular StackExchange site corresponding to these post links (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * A List of individual StackExchange {@link PostLink} objects
   */
  protected List<PostLink> postLinks;

  private Map<Integer, Set<PostLink>> postIdToPostLinkMap;

  /**
   * @return {@link PostLinks#site site}
   */
  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  /**
   * @return {@link PostLinks#postLinks postLinks}
   */
  public List<PostLink> getPostLinks() {
    return postLinks;
  }

  @XmlElement(name = StackExchangeConstants.XML_ELEM_POST_LINK) public void setPostLinks(List<PostLink> postLinks) {
    this.postLinks = postLinks;
  }

  /**
   * @param postId - The {@link Post#id ID} of the {@link Post} whose links are to be retrieved
   * @return A Set of {@link PostLink} objects, or <code>null</code> if no links exist for this post
   *         ID
   */
  public Set<PostLink> getLinksByPostId(Integer postId) {
    // Start by checking if we've populated a map of <post ID, links> pairs.
    // If not, populate that map first
    if (postIdToPostLinkMap == null) {
      populateMap();
    }
    return (postIdToPostLinkMap == null) ? null : postIdToPostLinkMap.get(postId);
  }

  /**
   * Populate a map of <post ID,links> pairs
   */
  private void populateMap() {
    if (postLinks == null)
      return; // There's nothing to do yet
    else
      postIdToPostLinkMap = new HashMap<Integer, Set<PostLink>>(postLinks.size());

    for (PostLink link : postLinks) {
      if (postIdToPostLinkMap.containsKey(link.getPostId()))
        postIdToPostLinkMap.get(link.getPostId()).add(link);
      else {
        HashSet<PostLink> postLinkSet = new HashSet<PostLink>(8);
        postLinkSet.add(link);
        postIdToPostLinkMap.put(link.getPostId(), postLinkSet);
      }
    }
  }

}
