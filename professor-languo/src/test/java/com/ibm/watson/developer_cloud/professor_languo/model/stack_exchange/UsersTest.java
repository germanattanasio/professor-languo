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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.User;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Users;

/**
 * Unit tests of the ability to unmarshal a StackExchange Users.xml file, translating each row of
 * the file into a distinct {@link User} object, populating the relevant fields
 *
 */
public class UsersTest {

  UsersTest GIVEN = this, WHEN = this, AND = this, THEN = this;

  private final static Logger logger = LogManager.getLogger(UsersTest.class.getName());

  private File inputUsersFile;
  private List<User> referenceUsers;
  private Users unmarshalledUsers;

  /**
   * This is not an actual unit test, but rather just a way to invoke unmarshalling of a complete
   * Users.xml file
   */
  // @Test
  public void test_import_complete_users_xml_file() throws JAXBException {
    File file = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleUsers.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    Users users = (Users) jaxbUnmarshaller.unmarshal(file);
    logger.info("# of votes unmarshalled: " + users.getUsers().size());
    int i = 1;
    for (User user : users.getUsers()) {
      logger.info("User " + i + "\n" + user.toString());
      i++;
    }
  }

  @Test public void test_that_users_xml_is_unmarshalled_correctly() throws JAXBException {
    GIVEN.input_users_xml_file_is_created();
    AND.reference_users_are_created();
    WHEN.users_xml_file_is_unmarshalled();
    THEN.unmarshalled_users_match_reference_users();
  }

  @Test public void test_that_id_to_user_map_is_populated_correctly() throws JAXBException {
    GIVEN.input_users_xml_file_is_created();
    AND.reference_users_are_created();
    WHEN.users_xml_file_is_unmarshalled();
    THEN.id_to_user_map_is_populated_correctly();
  }

  @Test public void test_that_deserialized_user_equals_original_object() throws JAXBException, IngestionException {
    GIVEN.input_users_xml_file_is_created();
    WHEN.users_xml_file_is_unmarshalled();
    THEN.unmarshalled_users_should_match_deserialized_users();

  }

  private void unmarshalled_users_should_match_deserialized_users() throws IngestionException {
    for (User user : unmarshalledUsers.getUsers()) {
      byte[] binCode = StackExchangeThreadSerializer.serializeObjToBinArr(user);
      User deserializedUser = (User) StackExchangeThreadSerializer.deserializeObjFromBinArr(binCode);
      assertTrue(user.equals(deserializedUser));
      assertEquals(user.hashCode(), deserializedUser.hashCode());
    }
  }

  private void id_to_user_map_is_populated_correctly() {
    for (User user : referenceUsers) {
      assertTrue("ID-to-User map is missing entry for user ID " + user.getId(),
          unmarshalledUsers.getUserById(user.getId()).equals(user));
    }
  }

  private void unmarshalled_users_match_reference_users() {
    for (int i = 0; i < referenceUsers.size(); i++) {
      assertTrue("Mismatch between unmarshalled user " + i + " and reference user " + i,
          unmarshalledUsers.getUsers().get(i).equals(referenceUsers.get(i)));
    }
  }

  private void users_xml_file_is_unmarshalled() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    unmarshalledUsers = (Users) jaxbUnmarshaller.unmarshal(inputUsersFile);
    unmarshalledUsers.setSite("robotics.stackexchange.com");
  }

  private void reference_users_are_created() {
    DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();

    User user0 = new User();
    user0.id = 9216;
    user0.reputation = 1;
    user0.displayName = "user9067";
    user0.views = 5;
    user0.upVotes = 10;
    user0.downVotes = 12;
    user0.accountId = 5763590;
    user0.websiteUrl = "http://www.test.com";
    user0.aboutMe = "<p>Sample blurb</p>";
    user0.creationDate = fmt.parseDateTime("2015-02-10T06:11:56.880");
    user0.lastAccessDate = fmt.parseDateTime("2015-02-11T05:01:32.937");

    User user1 = new User();
    user1.id = 9214;
    user1.reputation = 6;
    user1.displayName = "Koen";
    user1.views = 0;
    user1.upVotes = 100;
    user1.downVotes = 2;
    user1.accountId = 5763545;
    user1.age = 37;
    user1.creationDate = fmt.parseDateTime("2015-02-10T06:22:03.373");
    user1.lastAccessDate = fmt.parseDateTime("2015-03-08T01:49:26.080");

    User user2 = new User();
    user2.id = 9220;
    user2.reputation = 5;
    user2.displayName = "Justin";
    user2.views = 0;
    user2.upVotes = 200;
    user2.downVotes = 4;
    user2.accountId = 2629;
    user2.age = 31;
    user2.creationDate = fmt.parseDateTime("2015-02-10T06:22:03.373");
    user2.lastAccessDate = fmt.parseDateTime("2015-03-08T01:49:26.080");

    referenceUsers = Arrays.asList(user0, user1, user2);
  }

  private void input_users_xml_file_is_created() {
    inputUsersFile = new File(CorpusBuilderTest.class.getResource("/").getPath() + "sampleCorpus/SampleUsers.xml");
  }

}
