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
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This container class is used to store a list of StackExchange {@link User Users} (stored in a
 * Users.xml file)
 *
 */
@XmlRootElement(name = StackExchangeConstants.XML_ELEM_USERS)
public class Users {

  /**
   * The particular StackExchange site corresponding to these votes (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * A List of individual StackExchange {@link User} objects
   */
  protected List<User> users;

  private Map<Integer, User> idToUserMap;

  /**
   * @return {@link Users#site site}
   */
  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  /**
   * @return {@link Users#users users}
   */
  public List<User> getUsers() {
    return users;
  }

  @XmlElement(name = StackExchangeConstants.XML_ELEM_USER) public void setUsers(List<User> users) {
    this.users = users;
  }

  /**
   * @param id - The ID of the user, i.e., {@link User#id}
   * @return The {@link User} with the corresponding ID, or <code>null</code> if no such user exists
   */
  public User getUserById(Integer id) {
    // Start by checking to see if we've ever populated a map of <ID,User>
    // pairs. If so,
    // grab the User from the Map. If not, first populate the Map;
    if (idToUserMap == null) {
      populateMap();
    }
    return (idToUserMap == null) ? null : idToUserMap.get(id);
  }

  /**
   * Uses the list of Users to populate a map of <ID,User> pairs
   */
  private void populateMap() {
    if (users == null)
      return; // The list of users hasn't been set, so there's nothing to
    // do
    else
      idToUserMap = new HashMap<Integer, User>(users.size());

    idToUserMap = new HashMap<Integer, User>((int) (users.size() / 0.75 + 1));
    for (User user : users) {
      idToUserMap.put(user.getId(), user);
    }
  }

}
