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
 * This container class is used to store a list of StackExchange posts (i.e., questions and
 * answers), (stored in a Posts.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_POSTS)
public class Posts implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3386471385536000209L;

  /**
   * The particular StackExchange site corresponding to these posts (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * A List of individual StackExchange {@link Post} objects
   */
  protected List<Post> posts;

  private Map<Integer, Set<Post>> parentIdToPostsMap;

  /**
   * @return {@link Posts#site site}
   */
  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  /**
   * @return {@link Posts#posts posts}
   */
  public List<Post> getPosts() {
    return posts;
  }

  @XmlElement(name = StackExchangeConstants.XML_ELEM_POST) public void setPosts(List<Post> posts) {
    this.posts = posts;
  }

  /**
   * @param parentId - The ID of the parent post, or <code>null</code> to retrieve all posts without
   *        parents (i.e., questions)
   * @return A Set of all {@link Post} objects with the given parent ID, or <code>null</code> if no
   *         such parent ID exists
   */
  public Set<Post> getPostsByParentId(Integer parentId) {
    // Start by checking to see if we've ever populated a map of
    // <parentID,posts> pairs. If so,
    // grab the posts from the Map. If not, first populate the Map;
    if (parentIdToPostsMap == null) {
      populateMap();
    }
    return (parentIdToPostsMap == null) ? null : parentIdToPostsMap.get(parentId);
  }

  /**
   * Populates a map of <parent ID, posts> pairs, treating a <code>null</code> parent ID as the ID
   * for parent-less posts (i.e., questions)
   */
  private void populateMap() {
    if (posts == null)
      return; // There's nothing to do yet
    else
      parentIdToPostsMap = new HashMap<Integer, Set<Post>>(posts.size() / 3);

    for (Post post : posts) {
      if (parentIdToPostsMap.containsKey(post.getParentId()))
        parentIdToPostsMap.get(post.getParentId()).add(post);
      else {
        HashSet<Post> postSet = new HashSet<Post>(8);
        postSet.add(post);
        parentIdToPostsMap.put(post.getParentId(), postSet);
      }
    }
  }
}
