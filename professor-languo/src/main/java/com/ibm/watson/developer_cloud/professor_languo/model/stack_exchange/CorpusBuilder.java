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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.Indexer;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.StackExchangeThreadSerializer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.LinkType;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.QuestionSetManager;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.SingletonAnalyzer;

public class CorpusBuilder {

  private String postsXmlFile, usersXmlFile, votesXmlFile, postLinksXmlFile, site;
  private String dupThreadDirPath = null, uniqueThreadPath = null;
  private CSVPrinter csvPrinter = null;

  /**
   * The hashMap to find the original post id given an id of a duplicate thread
   */
  private Map<Integer, Integer> dupOrigIdMap = new HashMap<Integer, Integer>();

  public static final String TSV_COL_HEADER_THREAD_ID = "ThreadId";
  public static final String TSV_COL_HEADER_QUESTION_TITLE = "QuestionTitle";
  public static final String TSV_COL_HEADER_QUESTION_BODY = "QuestionBody";
  public static final String TSV_COL_HEADER_PARENT_ID = "ParentId";
  public static final String TSV_COL_HEADER_SERIALIZED_FILE_PATH = "SerFilePath";
  public static final String TSV_COL_HEADER_TAGS = "Tags";

  public CorpusBuilder() {
    super();
  }

  /**
   * This method parses a collection of StackExchange Internet Archive data dump files, and creates
   * a set of binary files of the {@link StackExchangeThread StackExchangeQuestions}---objects that
   * represent individual questions on a StackExchange site, along with their answers, and relevant
   * metadata.
   *
   * @return the path of the folder which stores the serialized newly-built StackExchangeThreads
   * @throws IngestionException
   */
  public String buildCorpus() throws IngestionException {
    return buildCorpus(postsXmlFile, usersXmlFile, votesXmlFile, postLinksXmlFile, site);
  }

  /**
   * This method parses a collection of StackExchange Internet Archive data dump files, and creates
   * a set of a set of binary files of the {@link StackExchangeThread StackExchangeQuestions}
   * ---objects that represent individual questions on a StackExchange site, along with their
   * answers, and relevant metadata.
   *
   * @param postsXmlFile - Path to a <code>Posts.xml</code></code> file
   * @param usersXmlFile - Path to a <code>Users.xml file
   * @param votesXmlFile - Path to a <code>Votes.xml</code> file
   * @param postLinksXmlFile - Path to a <code>PostLinks.xml</code> file
   * @param site - The name/URL of the StackExchange site, e.g., <code>"stackoverflow.com"</code>
   * @return the path of the folder which stores the serialized newly-built StackExchangeThreads
   * @throws IngestionException
   */
  public String buildCorpus(String postsXmlFile, String usersXmlFile, String votesXmlFile, String postLinksXmlFile,
      String site) throws IngestionException {
    File postsFile = new File(postsXmlFile);
    File usersFile = new File(usersXmlFile);
    File votesFile = new File(votesXmlFile);
    File postLinksFile = new File(postLinksXmlFile);

    return buildCorpus(postsFile, usersFile, votesFile, postLinksFile, site);
  }

  /**
   * This method parses a collection of StackExchange Internet Archive data dump files, and creates
   * a set of a set of binary files of the {@link StackExchangeThread StackExchangeQuestions}
   * ---objects that represent individual questions on a StackExchange site, along with their
   * answers, and relevant metadata.
   *
   * @param postsXmlFile - A <code>Posts.xml</code></code> file
   * @param usersXmlFile - A <code>Users.xml file
   * @param votesXmlFile - A <code>Votes.xml</code> file
   * @param postLinksXmlFile - A <code>PostLinks.xml</code> file
   * @param site - The name/URL of the StackExchange site, e.g., <code>"stackoverflow.com"</code>
   * @return the path of the folder which stores the serialized newly-built StackExchangeThreads
   * @throws IngestionException
   */
  public String buildCorpus(File postsXmlFile, File usersXmlFile, File votesXmlFile, File postLinksXmlFile, String site)
      throws IngestionException {

    try {
      Posts posts = unmarshallFile(postsXmlFile, Posts.class);
      Users users = unmarshallFile(usersXmlFile, Users.class);
      Votes votes = unmarshallFile(votesXmlFile, Votes.class);
      PostLinks postLinks = unmarshallFile(postLinksXmlFile, PostLinks.class);

      posts.setSite(site);
      users.setSite(site);
      votes.setSite(site);
      postLinks.setSite(site);

      return buildCorpus(posts, users, votes, postLinks, site);
    } catch (JAXBException | IngestionException e) {
      throw new IngestionException(e);
    }
  }

  /**
   * Builds a set of {@link StackExchangeThread} objects from the underlying {@link Posts},
   * {@link Users}, {@link Votes}, and {@link PostLinks} data-structures
   * 
   * @param posts - A {@link Posts} object for this corpus
   * @param users - A {@link Users} object for this corpus
   * @param votes - A {@link Votes} object for this corpus
   * @param postLinks - A {@link PostLinks} object for this corpus
   * @param site - The name/URL of the StackExchange site, e.g., <code>"stackoverflow.com"</code>
   * @return the path of the folder which stores the serialized newly-built StackExchangeThreads
   * @throws IngestionException
   */
  public String buildCorpus(Posts posts, Users users, Votes votes, PostLinks postLinks, String site)
      throws IngestionException {
    CSVPrinter csvPrinter = getCsvPrinter(dupThreadDirPath);// TODO refactor
    // the resource
    // open
    buildDupOrigMap(posts, postLinks);
    for (Post questionPost : posts.getPostsByParentId(null)) {
      StackExchangeThread thread =
          new StackExchangeThread(questionPost, users.getUserById(questionPost.getOwnerUserId()),
              buildAnswersForQuestion(questionPost.getId(), posts, users, votes, postLinks, site), site,
              votes.getVotesByPostId(questionPost.getId()), postLinks.getLinksByPostId(questionPost.getId()));

      if (thread.isDuplicate()) {
        int origId = findOriginalThreadId(thread.getId());
        if (origId != -1) // only save the duplicate thread which we can
          // find the corresponding
          // original thread
          saveDupThreadToDupFolder(csvPrinter, thread, origId);
      } else
        saveUniqThreadToUniqFolder(thread);
    }

    closeCsvPrinter();
    return uniqueThreadPath;
  }

  /**
   * Builds a set of {@link StackExchangeAnswer} objects from the underlying {@link Posts},
   * {@link Users}, {@link Votes}, and {@link PostLinks} data-structures for a given question ID
   * 
   * @param questionID - The ID of the question post
   * @param posts - A {@link Posts} object for this corpus
   * @param users - A {@link Users} object for this corpus
   * @param votes - A {@link Votes} object for this corpus
   * @param postLinks - A {@link PostLinks} object for this corpus
   * @param site - The name/URL of the StackExchange site, e.g., <code>"stackoverflow.com"</code>
   * @return A set of {@link StackExchangeAnswer} objects
   */
  public Set<StackExchangeAnswer> buildAnswersForQuestion(Integer questionID, Posts posts, Users users, Votes votes,
      PostLinks postLinks, String site) {

    Set<Post> answerPosts = posts.getPostsByParentId(questionID);

    if (answerPosts == null)
      return new HashSet<>(0); // There are no answers for this post ->
    // return empty set

    Set<StackExchangeAnswer> answerSet = new HashSet<StackExchangeAnswer>(answerPosts.size());
    // Iterate over each post that answers this question, building up
    // the StackExchangeAnswer
    for (Post answerPost : answerPosts) {
      // if (users.getUserById(answerPost.getOwnerUserId()) == null)
      // logger.info("No user for post " + answerPost.getId());

      // Build the StackExchangeAnswer...
      StackExchangeAnswer answer = new StackExchangeAnswer(answerPost, users.getUserById(answerPost.getOwnerUserId()),
          site, votes.getVotesByPostId(answerPost.getId()), postLinks.getLinksByPostId(answerPost.getId()));

      // ...and add it to the set
      answerSet.add(answer);
    }

    return answerSet;
  }

  /**
   * Unmarshalls (deserializes) a single XML file using JAXB
   *
   * @param xmlFile - The XML file to be unmarshalled
   * @param clazz - The class of object that <code>xmlFile</code> should be unmarshalled to
   * @return An object of class <code>clazz</code>, populated from <code>xmlFile</code>
   * @throws JAXBException
   */
  @SuppressWarnings("unchecked") public static <T> T unmarshallFile(File xmlFile, Class<T> clazz) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (T) jaxbUnmarshaller.unmarshal(xmlFile);
  }

  public void initialize(Properties corpusConfig) {
    String xmlDirPath = corpusConfig.getProperty(ConfigurationConstants.CORPUS_XML_DIR) + File.separator;
    postsXmlFile = xmlDirPath + corpusConfig.getProperty(ConfigurationConstants.CORPUS_POSTS_XML_FILENAME);
    usersXmlFile = xmlDirPath + corpusConfig.getProperty(ConfigurationConstants.CORPUS_USERS_XML_FILENAME);
    votesXmlFile = xmlDirPath + corpusConfig.getProperty(ConfigurationConstants.CORPUS_VOTES_XML_FILENAME);
    postLinksXmlFile = xmlDirPath + corpusConfig.getProperty(ConfigurationConstants.CORPUS_POSTLINKS_XML_FILENAME);
    site = corpusConfig.getProperty(ConfigurationConstants.CORPUS_SITE_NAME);

    dupThreadDirPath = corpusConfig.getProperty(ConfigurationConstants.DUPLICATE_THREAD_DIR) + File.separator;
    uniqueThreadPath = corpusConfig.getProperty(ConfigurationConstants.UNIQUE_THREAD_SER_PATH) + File.separator;

    SingletonAnalyzer.generateAnalyzer(corpusConfig.getProperty(ConfigurationConstants.ANALYZER));

  }

  /**
   * Deserialize the binary file of the built {@link StackExchangeThread}s stored in the
   * {@link CorpusBuilder#uniqueThreadPath}
   * 
   * @return - Set of the built {@link StackExchangeThread}
   * @throws IngestionException
   */
  public Set<StackExchangeThread> getUniqueThreadSetFromBinFiles() throws IngestionException {
    File[] serFiles = new File(uniqueThreadPath).listFiles();
    Set<StackExchangeThread> builtCorpus = new HashSet<StackExchangeThread>();
    for (File serFile : serFiles) {
      if (!serFile.getName().endsWith(".ser"))
        continue;// deserialize only the .ser files
      String fileName = serFile.getPath();
      builtCorpus.add(StackExchangeThreadSerializer.deserializeThreadFromBinFile(fileName));
    }
    return builtCorpus;
  }

  /**
   * Deserialize the binary file of the duplicate {@link StackExchangeThread}s stored in the
   * {@link CorpusBuilder#dupThreadDirPath}
   * 
   * @return - Set of the duplicate {@link StackExchangeThread}
   * @throws IngestionException
   */
  public Set<StackExchangeThread> getDupThreadSetFromBinFiles() throws IngestionException {
    File[] serFiles = new File(dupThreadDirPath).listFiles();
    Set<StackExchangeThread> builtCorpus = new HashSet<StackExchangeThread>();
    for (File serFile : serFiles) {
      if (!serFile.getName().endsWith(".ser"))
        continue;// deserialize only the .ser files
      String fileName = serFile.getAbsolutePath();
      StackExchangeThread thread = StackExchangeThreadSerializer.deserializeThreadFromBinFile(fileName);
      builtCorpus.add(thread);
    }
    return builtCorpus;
  }

  /**
   * Get the path of the folder which stores the duplicate threads information. Used by
   * {@link QuestionSetManager} to produce training, test, and validation question sets.
   * 
   * @return - the path of the folder which stores the duplicate threads information.
   */
  public String getDupThreadDirPath() {
    return dupThreadDirPath;
  }

  /**
   * Get the path of the folder which stores the unique/original threads information. Used by
   * {@link Indexer} to create index by deserializing the unique/original threads
   * 
   * @return - the path of the folder which stores the unique/original threads information.
   */
  public String getUniqueThreadDirPath() {
    return uniqueThreadPath;
  }

  /**
   * Given the post id of the duplicate {@link StackExchangeThread}, find the original post id.
   * 
   * @param dupId - the post id of the duplicate {@link StackExchangeThread}
   * @return the original thread post id , or -1 if the unique original thread cannot be found
   */
  public int findOriginalThreadId(int id) {
    Set<Integer> footprint = new HashSet<Integer>();
    while (dupOrigIdMap.containsKey(id)) {
      if (footprint.contains(id))
        return -1;
      footprint.add(id);
      id = dupOrigIdMap.get(id);
    }
    return id;
  }

  /**
   * Store the newly-created {@link StackExchangeThread} in the binary file because the real corpus
   * consumes such a huge memory that the cluster cannot afford to keep the built
   * {@link StackExchangeThread}s as a Java Collection in memory.
   * 
   * @param thread - the newly-created {@link StackExchangeThread}
   * @throws IngestionException
   */
  private void saveUniqThreadToUniqFolder(StackExchangeThread thread) throws IngestionException {
    StackExchangeThreadSerializer.serializeThreadToBinFile(thread, uniqueThreadPath);
  }

  /**
   * Create a binary serialized file for the duplicate {@link StackExchangeThread} and append the
   * key fields of the duplicate threads in the TSV file
   * 
   * @param csvPrinter - specify which file is the TSV file and how to write it(delimit the fields
   *        with tabs)
   * @param dupThread - the duplicate {@link StackExchangeThread} to be saved both in the binary
   *        file and appended to the TSV file
   * @throws IngestionException
   */
  private void saveDupThreadToDupFolder(CSVPrinter csvPrinter, StackExchangeThread dupThread, int origId)
      throws IngestionException {
    String serFileName =
        dupThreadDirPath + StackExchangeThreadSerializer.serializeThreadToBinFile(dupThread, dupThreadDirPath);
    StackExchangeThreadSerializer.serializeDupThreadToTsvFile(csvPrinter, dupThread, origId, serFileName);
  }

  /**
   * Build the duplicate-original relationship map for the duplicate {@link StackExchangeThread}s,
   * the {@link CorpusBuilder#dupOrigIdMap} is used to associate a duplicate
   * {@link StackExchangeThread} with its original {@link StackExchangeThread}
   * 
   * @param posts - all the posts built from the xml post file
   * @param postLinks - all the post links built from the xml post links file
   */
  public void buildDupOrigMap(Posts posts, PostLinks postLinks) {
    // for each question post
    for (Post questionPost : posts.getPostsByParentId(null)) {
      int questionPostId = questionPost.getId();
      Set<PostLink> linksOfPost = postLinks.getLinksByPostId(questionPostId);
      if (linksOfPost != null)
        for (PostLink link : linksOfPost)
          if (link.getLinkType().equals(LinkType.DUPLICATE))
            dupOrigIdMap.put(questionPostId, link.getRelatedPostId());
    }
  }

  public void deleteUniqThreadFolder() throws IngestionException {
    File fileToDelete = null;
    try {
      fileToDelete = new File(getUniqueThreadDirPath());
      FileUtils.deleteDirectory(fileToDelete);
    } catch (IOException e) {
      throw new IngestionException(e);
    }
  }

  /**
   * get the csv printer if it has been created , create a new one if the csv printer doesn't exist.
   * The reason of doing so is to allow multiple duplicate threads to be serialized to the same TSV
   * file without overwriting each other
   * 
   * @param tsvDir - the folder containing the TSV file
   * @return the csv printer used to write to the TSV file
   * @throws IngestionException
   */
  public CSVPrinter getCsvPrinter(String tsvDir) throws IngestionException {
    if (csvPrinter == null) {
      try {
        String tsvFilePath = tsvDir + StackExchangeConstants.DUP_THREAD_TSV_FILE_NAME
            + StackExchangeConstants.DUP_THREAD_TSV_FILE_EXTENSION;
        File csvFile = new File(tsvFilePath);
        if (csvFile.getParentFile() != null)
          csvFile.getParentFile().mkdirs();
        if (!csvFile.exists())
          csvFile.createNewFile();

        PrintWriter writer = new PrintWriter(new FileWriter(tsvFilePath, true));
        csvPrinter = new CSVPrinter(writer, CSVFormat.TDF.withHeader(getTsvColumnHeaders()));
      } catch (IOException e) {
        throw new IngestionException(e);
      }
      return csvPrinter;
    }
    return csvPrinter;
  }

  public static String[] getTsvColumnHeaders() {
    return new String[] {TSV_COL_HEADER_THREAD_ID, TSV_COL_HEADER_QUESTION_TITLE, TSV_COL_HEADER_QUESTION_BODY,
        TSV_COL_HEADER_PARENT_ID, TSV_COL_HEADER_SERIALIZED_FILE_PATH, TSV_COL_HEADER_TAGS};
  }

  /**
   * Close the csvPrinter after writing the TSV file
   * 
   * @throws IngestionException
   */
  public void closeCsvPrinter() throws IngestionException {
    if (csvPrinter != null)
      try {
        csvPrinter.close();
      } catch (IOException e) {
        throw new IngestionException(e);
      }
  }

  /**
   * print every field of the tsvFile serialized from the duplicate threads
   * 
   * @param tsvDir - Directory containing the duplicate thread TSV file
   * @throws IngestionException
   */
  public static void printOutDupThreadCsvFile(String tsvDir) throws IngestionException {
    BufferedReader br = null;

    String txtFilePath =
        tsvDir + StackExchangeConstants.DUP_THREAD_TSV_FILE_NAME + StackExchangeConstants.DUP_THREAD_TSV_FILE_EXTENSION;
    try {
      br = new BufferedReader(new FileReader(txtFilePath));

      String line = null;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
      br.close();
    } catch (IOException e) {
      throw new IngestionException(e);
    }
  }

}
