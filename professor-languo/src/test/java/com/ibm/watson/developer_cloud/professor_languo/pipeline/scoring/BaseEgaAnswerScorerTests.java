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

package com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Post;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.User;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.Vote;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;

/**
 * A base class for testing EGA Answer Scorers
 *
 */
public class BaseEgaAnswerScorerTests {
  BaseEgaAnswerScorerTests GIVEN = this, WHEN = this, THEN = this, OR = this, AND = this;

  public StackExchangeQuestion question1, question2;
  CandidateAnswer[] candidateAnswers;
  public Observable<CandidateAnswer> question1CandidateAnswers;
  private static final String SITE_STRING = "stackoverflow.com";

  public void sample_input_is_created() {
    // START QUESTION 1
    List<String> questionTags = new ArrayList<String>();
    questionTags.add("tag1");
    questionTags.add("tag3");
    questionTags.add("tag5");
    questionTags.add("tag7");
    questionTags.add("tag9");
    question1 = new StackExchangeQuestion(
        "What is the Answer to the Ultimate Question of Life" + ", the Universe, and Everything?", null, questionTags,
        42, "");

    candidateAnswers = new CandidateAnswer[] {constructQuestion1CandidateAnswer1(),
        constructQuestion1CandidateAnswer2(), constructQuestion1CandidateAnswer3()};
    question1CandidateAnswers = Observable.from(candidateAnswers);
  }

  // ****************Start question1 CandidateAnswers ***********************
  private StackExchangeThread constructQuestion1CandidateAnswer1() {
    StackExchangeThread candidateAnswer;
    Post candidateAnswerQuestionPost;
    List<StackExchangeAnswer> stackExchangeAnswers;
    StackExchangeAnswer sEAnswer;
    Post sEAnswerPost;
    User sEAnswerUser;
    List<Vote> votesList;
    Vote vote;

    candidateAnswerQuestionPost = new Post();
    candidateAnswerQuestionPost.setId(1);
    candidateAnswerQuestionPost.setTitle("Does anyone have an answer to the ultimate question of life?");
    candidateAnswerQuestionPost.setAcceptedAnswerId(999_1);
    candidateAnswerQuestionPost.setViewCount(1000);
    List<String> answerTags = new ArrayList<String>();
    answerTags.add("tag1");
    answerTags.add("tag2");
    answerTags.add("tag3");
    answerTags.add("tag4");
    answerTags.add("tag5");
    candidateAnswerQuestionPost.setTags(answerTags);

    stackExchangeAnswers = new ArrayList<>();

    sEAnswerPost = new Post();
    sEAnswerPost.setId(999_1);
    sEAnswerPost.setBody("No, there is no answer to the ultimate question of life! What's wrong with you? ...");
    sEAnswerPost.setViewCount(1_900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(999_1);
    sEAnswerUser.setReputation(3200);

    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer1

    votesList = new ArrayList<>();
    vote = new Vote();// vote 1
    vote.setId(999_1);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_1);
    votesList.add(vote);

    vote = new Vote();// vote 2
    vote.setId(999_2);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_2);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(999_3);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_3);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(999_3);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_32);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 1

    sEAnswerPost = new Post();
    sEAnswerPost.setId(999_2);
    sEAnswerPost.setBody("The cosmos will present to us all its secrets when it is ready. Be patient, young one.");
    sEAnswerPost.setViewCount(200_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(999_2);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer2

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(999_4);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_4);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_5);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_5);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_6);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_6);
    votesList.add(vote);

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(999_7);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_7);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer2

    sEAnswerPost = new Post();
    sEAnswerPost.setId(999_3);
    sEAnswerPost.setBody("Waldo is neither here nor there. Waldo is everywhere ...");
    sEAnswerPost.setViewCount(900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(999_3);
    sEAnswerUser.setReputation(32_100);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer3

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(999_8);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_8);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_9);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_9);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_10);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_10);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_11);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_11);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_12);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_12);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(999_13);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_13);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 3

    sEAnswerPost = new Post();
    sEAnswerPost.setId(999_4);
    sEAnswerPost.setBody("NOOOOO! I don't have any answers for you ...");
    sEAnswerPost.setViewCount(1_900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(999_4);
    sEAnswerUser.setReputation(3200);

    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer1

    votesList = new ArrayList<>();
    vote = new Vote();// vote 1
    vote.setId(999_14);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_14);
    votesList.add(vote);

    vote = new Vote();// vote 2
    vote.setId(999_15);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_15);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(999_16);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_16);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(999_17);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_17);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 4

    sEAnswerPost = new Post();
    sEAnswerPost.setId(999_5);
    sEAnswerPost.setBody("Hmmm, let me ask a Greek God ...");
    sEAnswerPost.setViewCount(1_900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(999_5);
    sEAnswerUser.setReputation(3200);

    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer1

    votesList = new ArrayList<>();
    vote = new Vote();// vote 1
    vote.setId(999_18);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_18);
    votesList.add(vote);

    vote = new Vote();// vote 2
    vote.setId(999_19);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(999_19);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 5

    User candidateAnswer1Author = new User();
    candidateAnswer1Author.setId(999);
    candidateAnswer1Author.setReputation(10_000);
    candidateAnswer =
        new StackExchangeThread(candidateAnswerQuestionPost, candidateAnswer1Author, stackExchangeAnswers, null);
    candidateAnswer.setConfidence(1.0);
    return candidateAnswer;
  }

  private StackExchangeThread constructQuestion1CandidateAnswer2() {
    StackExchangeThread candidateAnswer;
    Post candidateAnswerQuestionPost;
    List<StackExchangeAnswer> stackExchangeAnswers;
    StackExchangeAnswer sEAnswer;
    Post sEAnswerPost;
    User sEAnswerUser;
    List<Vote> votesList;
    Vote vote;

    candidateAnswerQuestionPost = new Post();
    candidateAnswerQuestionPost.setId(1);
    candidateAnswerQuestionPost.setTitle("When will we have the answer to the ultimate question of life?");
    candidateAnswerQuestionPost.setAcceptedAnswerId(998_1);
    List<String> answerTags = new ArrayList<String>();
    answerTags.add("tag4");
    answerTags.add("tag5");
    answerTags.add("tag6");
    answerTags.add("tag7");
    answerTags.add("tag8");
    candidateAnswerQuestionPost.setTags(answerTags);

    stackExchangeAnswers = new ArrayList<>();
    sEAnswerPost = new Post();
    sEAnswerPost.setId(998_1);
    sEAnswerPost.setBody("The answer is already available to us all. All you have to do is open your eyes!");
    sEAnswerPost.setViewCount(1_900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(998_1);
    sEAnswerUser.setReputation(3200);

    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer1

    votesList = new ArrayList<>();
    vote = new Vote();// vote 1
    vote.setId(998_1);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_1);
    votesList.add(vote);

    vote = new Vote();// vote 2
    vote.setId(998_2);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_2);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(999_3);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_3);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 1

    sEAnswerPost = new Post();
    sEAnswerPost.setId(998_2);
    sEAnswerPost.setBody("Never will we be able to answer this question.");
    sEAnswerPost.setViewCount(200_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(998_2);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer2

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(998_4);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_4);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_5);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_5);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_6);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_6);
    votesList.add(vote);

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(998_7);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_7);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer2

    sEAnswerPost = new Post();
    sEAnswerPost.setId(998_3);
    sEAnswerPost.setBody(
        "Well what do you want from me? I don't have any answers for you, letalone one to the Ultimate Answer ...");
    sEAnswerPost.setViewCount(900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(998_3);
    sEAnswerUser.setReputation(32_100);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer3

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(998_8);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_8);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_9);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_9);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_10);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_10);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_11);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(998_11);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_12);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_12);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(998_13);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(998_13);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 3

    User candidateAnswer2Author = new User();
    candidateAnswer2Author.setId(998);
    candidateAnswer2Author.setReputation(9_000);

    candidateAnswer =
        new StackExchangeThread(candidateAnswerQuestionPost, candidateAnswer2Author, stackExchangeAnswers, SITE_STRING);
    candidateAnswer.setConfidence(1.0);
    return candidateAnswer;
  }

  private StackExchangeThread constructQuestion1CandidateAnswer3() {
    StackExchangeThread candidateAnswer;
    Post candidateAnswerQuestionPost;
    List<StackExchangeAnswer> stackExchangeAnswers;
    StackExchangeAnswer sEAnswer;
    Post sEAnswerPost;
    User sEAnswerUser;
    List<Vote> votesList;
    Vote vote;

    candidateAnswerQuestionPost = new Post();
    candidateAnswerQuestionPost.setId(1);
    candidateAnswerQuestionPost.setTitle("Looking for the Ultimate Answer to the Ultimate Question of the Universe?");
    candidateAnswerQuestionPost.setAcceptedAnswerId(997_1);
    List<String> answerTags = new ArrayList<String>();
    answerTags.add("tag2");
    answerTags.add("tag4");
    answerTags.add("tag6");
    answerTags.add("tag8");
    answerTags.add("tag0");
    candidateAnswerQuestionPost.setTags(answerTags);

    stackExchangeAnswers = new ArrayList<>();
    sEAnswerPost = new Post();
    sEAnswerPost.setId(997_1);
    sEAnswerPost.setBody("To answer the Ultinate Question of Life, we will need the Ultinate Stack of Life. "
        + "This stack shall not overflow in the presence of the Answer ... Bring me the Ultinate STack and we shall go to work.");
    sEAnswerPost.setViewCount(1_900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(997_1);
    sEAnswerUser.setReputation(3200);

    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer1

    votesList = new ArrayList<>();
    vote = new Vote();// vote 1
    vote.setId(997_1);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(999_1);
    votesList.add(vote);

    vote = new Vote();// vote 2
    vote.setId(997_2);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_2);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(997_3);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_3);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(997_32);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_32);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(997_33);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_33);
    votesList.add(vote);

    vote = new Vote();// vote 3
    vote.setId(997_34);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_34);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 1

    sEAnswerPost = new Post();
    sEAnswerPost.setId(997_2);
    sEAnswerPost.setBody("What is the Ultinate Question? Do you even have a clue as to what you are asking?");
    sEAnswerPost.setViewCount(200_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(997_2);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer2

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(997_4);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_4);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_5);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_5);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_6);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_6);
    votesList.add(vote);

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(997_7);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_7);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer2

    sEAnswerPost = new Post();
    sEAnswerPost.setId(997_3);
    sEAnswerPost.setBody("No, you need to go ask Dr. Tyson ...");
    sEAnswerPost.setViewCount(900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(997_3);
    sEAnswerUser.setReputation(32_100);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer3

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(997_8);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_8);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_9);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_9);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_10);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_10);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_11);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_11);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_12);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_12);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_13);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_13);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_14);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_14);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 3

    sEAnswerPost = new Post();
    sEAnswerPost.setId(997_4);
    sEAnswerPost.setBody("What do you mean by the ultimate question?");
    sEAnswerPost.setViewCount(900_000);
    sEAnswerUser = new User();
    sEAnswerUser.setId(997_4);
    sEAnswerUser.setReputation(32_100);
    sEAnswer = new StackExchangeAnswer(sEAnswerPost, sEAnswerUser, SITE_STRING);// candidateAnswer
    // answer4

    votesList = new ArrayList<>();
    vote = new Vote();
    vote.setId(997_15);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_15);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_16);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_16);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_17);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_17);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_18);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_18);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_19);
    vote.setVoteType(VoteType.UP_MOD);
    vote.setUserId(997_19);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_20);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_20);
    votesList.add(vote);

    vote = new Vote();
    vote.setId(997_21);
    vote.setVoteType(VoteType.DOWN_MOD);
    vote.setUserId(997_21);
    votesList.add(vote);

    sEAnswer.addVotesToMap(votesList);
    stackExchangeAnswers.add(sEAnswer);// add answer 4

    User candidateAnswer3Author = new User();// author
    candidateAnswer3Author.setId(997);
    candidateAnswer3Author.setReputation(8_500);

    candidateAnswer =
        new StackExchangeThread(candidateAnswerQuestionPost, candidateAnswer3Author, stackExchangeAnswers, SITE_STRING);
    candidateAnswer.setConfidence(1.0);
    return candidateAnswer;
  }
}
