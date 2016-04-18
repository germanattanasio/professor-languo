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

import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.data_model.TextWithAnalysis;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.LinkType;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.VoteType;

/**
 * This container class represents a single question on a Stack Exchange forum, and contains, in
 * addition to the underlying question {@link Post}, information about the {@link User} who authored
 * the post, all {@link StackExchangeAnswer StackExchangeAnswers} to the question, and any
 * {@link Vote Votes} or {@link PostLink PostLinks} associated with the question.
 *
 */
public class StackExchangeThread extends CandidateAnswer implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3259399186429682828L;

  /**
   * The particular StackExchange site corresponding to this question (e.g., stackoverflow.com)
   */
  protected String site;

  /**
   * The {@link Post} corresponding to this question
   */
  protected Post question;

  /**
   * The {@link User} who authored this question
   */
  protected User author;
  /**
   * A set of {@link StackExchangeAnswer StackExchangeAnswers} to this question
   */
  protected Set<StackExchangeAnswer> answers;

  /**
   * A map from a {@link VoteType} to a Set of {@link Vote} objects that were cast for this question
   */
  protected Map<VoteType, Set<Vote>> voteMap;

  /**
   * A list of {@link PostLink} links attached to this question
   */
  protected List<PostLink> postLinkList;

  /**
   * Create a new {@link StackExchangeThread}
   * 
   * @param question - The {@link Post} representing this question
   * @param author - The {@link User} who authored this question
   * @param answers - A collection of {@link StackExchangeAnswer StackExchangeAnswers} to this
   *        question
   * @param site - The URL of the StackExchange site hosting this answer (e.g., "stackoverflow.com")
   */
  public StackExchangeThread(Post question, User author, Collection<StackExchangeAnswer> answers, String site) {
    this(question, author, answers, site, null, null);
  }

  /**
   * Create a new {@link StackExchangeThread}
   * 
   * @param question - The {@link Post} representing this question
   * @param author - The {@link User} who authored this question
   * @param answers - A collection of {@link StackExchangeAnswer StackExchangeAnswers} to this
   *        question
   * @param site - The URL of the StackExchange site hosting this answer (e.g., "stackoverflow.com")
   * @param votes - A collection of {@link Vote Votes} associated with this question
   * @param links - A collection of {@link PostLink PostLinks} associated with this question
   */
  public StackExchangeThread(Post question, User author, Collection<StackExchangeAnswer> answers, String site,
      Collection<Vote> votes, Collection<PostLink> links) {
    // Use the site as the component ID
    this(question, author, answers, site, votes, links, site);
  }

  /**
   * Create a new {@link StackExchangeThread}
   * 
   * @param question - The {@link Post} representing this question
   * @param author - The {@link User} who authored this question
   * @param answers - A collection of {@link StackExchangeAnswer StackExchangeAnswers} to this
   *        question
   * @param site - The URL of the StackExchange site hosting this answer (e.g., "stackoverflow.com")
   * @param votes - A collection of {@link Vote Votes} associated with this question
   * @param links - A collection of {@link PostLink PostLinks} associated with this question
   * @param componentId - A string identifying the component that generated this
   *        {@link CandidateAnswer}
   */
  public StackExchangeThread(Post question, User author, Collection<StackExchangeAnswer> answers, String site,
      Collection<Vote> votes, Collection<PostLink> links, String componentId) {

    // Use the question post ID as the answer label
    super(question.getId().toString(), componentId);

    // Populate StackExchangeThread-specific fields
    this.question = question;
    this.author = author;
    this.site = site;
    this.answers = (answers == null) ? new HashSet<StackExchangeAnswer>(8) : new HashSet<StackExchangeAnswer>(answers);
    this.voteMap = new HashMap<StackExchangeConstants.VoteType, Set<Vote>>(20);
    if (votes != null)
      addVotesToMap(votes);
    this.postLinkList = new ArrayList<PostLink>();
    if (links != null)
      addPostLinks(links);

    // Populate CandidateAnswer-specific fields
    if (question != null) {
      this.answerTitle = new TextWithAnalysis(question.getTitle());
      this.answerDocumentText = new TextWithAnalysis(question.getBody());
    }
    if (answers != null) {
      Collection<TextWithAnalysis> answerTexts = new HashSet<>(answers.size());
      for (StackExchangeAnswer answerPost : answers) {
        if (answerPost != null && answerPost.getAnswer() != null)
          answerTexts.add(new TextWithAnalysis(answerPost.getAnswer().getBody()));
      }
      this.questionVariants = answerTexts;
    }
  }

  /**
   * @return The particular StackExchange site corresponding to this question (e.g.,
   *         "stackoverflow.com")
   */
  public String getSite() {
    return site;
  }

  /**
   * @return The {@link Post} corresponding to this question
   */
  public Post getQuestion() {
    return question;
  }

  /**
   * @return The {@link User} who authored this question
   */
  public User getAuthor() {
    return author;
  }

  /**
   * @return The set of {@link StackExchangeAnswer StackExchangeAnswers} to this question
   */
  public Set<StackExchangeAnswer> getAnswers() {
    return answers;
  }

  /**
   * @return A map from a {@link VoteType} to a Set of {@link Vote} objects that were cast for this
   *         question
   */
  public Map<VoteType, Set<Vote>> getVoteMap() {
    return voteMap;
  }

  /**
   * @return A list of {@link PostLink} links attached to this question
   */
  public List<PostLink> getPostLinkList() {
    return postLinkList;
  }

  /**
   * @return id - The unique ID of this question, i.e., {@link Post#id}
   */
  public int getId() {
    return question.getId();
  }

  /**
   * Get the accepted answer's text and return it to the document mapper as a field of the document
   * 
   * @return the accepted answer's text if any, or "" if no accepted answer
   */
  public String getAcceptedAnswerText() {
    if (getAcceptedAnswer() == null)
      return "";
    return getAcceptedAnswer().getAnswer().getUnformattedBody();
  }

  /**
   * Get the accepted answer for the {@link StackExchangeThread}
   * 
   * @return the accepted answer
   */
  public StackExchangeAnswer getAcceptedAnswer() {
    if (answers == null || question.acceptedAnswerId == null)
      return null;
    for (StackExchangeAnswer answer : answers) {
      if (answer.getId() == question.acceptedAnswerId)
        return answer;
    }
    return null;
  }

  /**
   * Get the top-voted answer's text and return it to the document mapper as a field of the document
   * 
   * @return the top-voted answer's text if any, or "" if the question has no answer
   */
  public String getTopVotedAnswerText() {
    if (answers == null)
      return "";
    StackExchangeAnswer topVotedAnswer = null;
    int topScore = Integer.MIN_VALUE;
    for (StackExchangeAnswer answer : answers) {
      int curScore = answer.getAnswer().getScore();
      if (curScore >= topScore) {
        topScore = curScore;
        topVotedAnswer = answer;
      }
    }
    return (topVotedAnswer == null) ? "" : topVotedAnswer.getAnswer().getUnformattedBody();
  }

  /**
   * Get the all the answers' concatenated text and return it to the document mapper as a field of
   * the document
   * 
   * @return the concatenated answers's text separated by newline character if any, or "" if the
   *         question has no answer
   */
  public String getConcatenatedAnswersText() {
    StringBuilder sb = new StringBuilder();
    for (StackExchangeAnswer answer : answers) {
      sb.append(answer.getAnswer().getUnformattedBody() + "\n");
    }
    return sb.toString();
  }

  /**
   * @return The tags for the thread, different tags are concatenated by a semicolon
   */
  public String getConcatenatedTagsText() {
    StringBuilder sb = new StringBuilder();
    boolean firstToken = true;
    for (String tag : getQuestion().getTags()) {
      if (firstToken) {
        firstToken = false;
        sb.append(tag);
      } else
        sb.append(";" + tag);
    }
    return sb.toString();
  }

  /**
   * @param answer - The {@link StackExchangeAnswer} to add to the set of answers to this question
   */
  public void addAnswer(StackExchangeAnswer answer) {
    answers.add(answer);
  }

  /**
   * @param answers - The collection of {@link StackExchangeAnswer} objects to add to the set of
   *        answers to this question
   */
  public void addAllAnswers(Collection<StackExchangeAnswer> answers) {
    for (StackExchangeAnswer answer : answers)
      addAnswer(answer);
  }

  /**
   * Add a new {@link Vote} to this question
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
   * Add a collection of {@link Vote} objects to this question
   * 
   * @param votes - The collection of {@link Vote} objects to add
   */
  public void addVotesToMap(Collection<Vote> votes) {
    for (Vote v : votes) {
      addVoteToMap(v);
    }
  }

  /**
   * Add a new {@link PostLink} to this question
   *
   * @param link - The PostLink to add
   */
  public void addPostLink(PostLink link) {
    postLinkList.add(link);
  }

  /**
   * Add a collection of {@link PostLink} objects to this question
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
   * @return The tally of votes of the specified type cast for this question
   */
  public int getVoteCount(VoteType voteType) {
    if (voteMap.containsKey(voteType))
      return voteMap.get(voteType).size();
    else
      return 0;
  }

  /**
   * @return <code>true</code> if this question was flagged as a duplicate of another
   *         {@link StackExchangeQuestion}, i.e., there is at least one {@link PostLink} whose
   *         {@link LinkType} == DUPLICATE
   */
  public boolean isDuplicate() {
    for (PostLink link : postLinkList) {
      if (link.getLinkType().equals(LinkType.DUPLICATE))
        return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override public String toString() {
    return "\n<StackExchangeQuestion>\n" + "\t[SITE]: " + site + "\n" + "\t[QUESTION]: "
        + indentString(printIfNotNull(question)) + "\t[AUTHOR]: " + indentString(printIfNotNull(author))
        + "\t[ANSWERS]: " + indentString(printIfNotNull(answers)) + "\t[VOTE MAP]: "
        + indentString(printIfNotNull(voteMap)) + "\t[POSTLINK LIST]: " + indentString(printIfNotNull(postLinkList));
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
    StackExchangeThread other = (StackExchangeThread) obj;
    if (answers == null) {
      if (other.answers != null)
        return false;
    } else if (!answers.equals(other.answers))
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
    if (question == null) {
      if (other.question != null)
        return false;
    } else if (!question.equals(other.question))
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
