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

package com.ibm.watson.developer_cloud.professor_languo.ingestion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;

import com.ibm.watson.developer_cloud.professor_languo.configuration.ConfigurationConstants;
import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.exception.SearchException;
import com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing.Indexer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.CorpusBuilder;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeQuestion;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeThread;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.PipelineConstants;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.answer_gen.StackExchangeThreadAnswerGenerator;

/**
 * 1) Create the Set<StackExchangeThread> objects from the raw Foo.xml files (with the user
 * providing the paths to the Foo.xml files as program arguments in the run configuration)
 * 
 * 2) Partition the set into duplicate and non-duplicate threads
 * 
 * 3) Write the non-duplicate threads to an index that lives on the filesystem (with the user
 * providing the path to the index as a program argument in the run configuration)
 * 
 * 4) Print the IndexingStats to the console and, optionally, write them to a .txt file (user
 * provides output path)
 * 
 * 5) Writes the postId, question title, question body, question tags and original thread post ID
 * (the thread that this thread duplicates, following the chain of A -> B -> C as needed) for every
 * duplicate question to a single tab-separated values file (user specifies where this .tsv file
 * should go)
 */
public class IngestionDriver {
  private final static Logger logger = LogManager.getLogger(IngestionDriver.class.getName());
  private static Options options = new Options();

  // paths of the resource files specified by the user from the command line
  private static String indexStatPath = null;
  private static String indexDirPath = null;
  private static String propertyFilePath = null;
  private static String dupThreadDirPath = null;
  private static String uniqThreadDirPath = null;
  private static String resDirPath = null;

  private static String queryGeneratorsStr = null;
  private static String candidateAnswerNumStr = null;
  private static StackExchangeQuestion[] queryQuestions = null;
  private static Map<String, List<String>> questionAnswerMap = new HashMap<String, List<String>>();
  private static StackExchangeThreadAnswerGenerator answerGenerator = null;

  // command line options are parsed into properties to be used to initialize
  // the CorpusBuilder and Indexer
  private static Properties appProperties = new Properties();
  private static Properties extraProperties = new Properties();

  private static CorpusBuilder corpusBuilder = null;
  private static Indexer indexer;

  public static void main(String[] args) throws IngestionException, SearchException {
    CommandLine cmdLine = createCmdLineInterpreter(args);
    parseCommandLineOptions(cmdLine);
    clear_res_directory();
    create_corpus_from_the_user_specified_xml();
    build_index_from_the_corpus_and_save_resource_files_to_the_user_specified_paths();
    /**
     * The following actions are commented out because they belong to the pipeline, which should be
     * performed after the ingestion phase. But you can uncommented them if you want to see how to
     * performance a search and its result based on the index we just build.
     */
    generate_query_questions();
    generate_queries_and_find_candidate_answers();
    print_out_answers_retrived_from_query();

  }

  private static void build_index_from_the_corpus_and_save_resource_files_to_the_user_specified_paths()
      throws IngestionException {
    indexer = IndexerAndSearcherFactory.getIndexer(appProperties);
    indexer.indexCorpus(uniqThreadDirPath);
    corpusBuilder.deleteUniqThreadFolder();
  }

  private static void create_corpus_from_the_user_specified_xml() throws IngestionException {
    corpusBuilder = new CorpusBuilder();
    corpusBuilder.initialize(appProperties);
    uniqThreadDirPath = corpusBuilder.buildCorpus();
    logger.info(Messages.getString("RetrieveAndRank.BUILD_CORPUS")); //$NON-NLS-1$
    logger.info(Messages.getString("RetrieveAndRank.PARTITION_SETS")); //$NON-NLS-1$
    logger.info(Messages.getString("RetrieveAndRank.WRITE_DUPLICATE_THREADS")); //$NON-NLS-1$
  }

  /**
   * Given the command line, parse the options within the command line to determine the behavior of
   * our program.
   * 
   * @param cmdLine
   * @throws IngestionException
   */
  private static void parseCommandLineOptions(CommandLine cmdLine) throws IngestionException {
    if (cmdLine.hasOption("i")) {
      indexStatPath = cmdLine.getOptionValue("i");
      extraProperties.put(ConfigurationConstants.INDEX_STAT_PATH, indexStatPath);
    }

    if (cmdLine.hasOption("d")) {
      indexDirPath = cmdLine.getOptionValue("d");
      extraProperties.put(ConfigurationConstants.INDEX_DIR, indexDirPath);
    }

    if (cmdLine.hasOption("v")) {
      dupThreadDirPath = cmdLine.getOptionValue("v");
      extraProperties.put(ConfigurationConstants.DUPLICATE_THREAD_TSV_PATH, dupThreadDirPath);
    }

    if (cmdLine.hasOption("u")) {
      uniqThreadDirPath = cmdLine.getOptionValue("u");
      extraProperties.put(ConfigurationConstants.UNIQUE_THREAD_SER_PATH, uniqThreadDirPath);
    }

    if (cmdLine.hasOption("r")) {
      resDirPath = cmdLine.getOptionValue("r");
      extraProperties.put(ConfigurationConstants.INGESTION_BASE_DIR, resDirPath);
    }

    if (cmdLine.hasOption("q")) {
      queryGeneratorsStr = cmdLine.getOptionValue("q");
      extraProperties.put(ConfigurationConstants.QUERY_GENERATORS, queryGeneratorsStr);
    }
    if (cmdLine.hasOption("b")) {
      queryGeneratorsStr = cmdLine.getOptionValue("b");
      extraProperties.put(ConfigurationConstants.CANDIDATE_ANSWER_NUM, candidateAnswerNumStr);
    }

    if (cmdLine.hasOption("c")) {
      propertyFilePath = cmdLine.getOptionValue("c");
    } else {
      propertyFilePath = "app_config.properties";
    }
    if (cmdLine.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(IngestionDriver.class.getSimpleName(), options);
    }

    try {
      appProperties.load(new FileInputStream(propertyFilePath));
    } catch (IOException e) {
      throw new IngestionException(e);
    }
    appProperties.putAll(extraProperties);
  }

  /**
   * create a CommandLine interpreter while is able to detect the options used by the user and get
   * the values of the options easily
   * 
   * @param args - the string tokens for the command line arguments
   * @return a CommandLine interpreter while is able to detect the options used by the user and get
   *         the values of the options easily
   * @throws IngestionException
   */
  @SuppressWarnings("static-access") private static CommandLine createCmdLineInterpreter(String[] args)
      throws IngestionException {
    CommandLine cmdLine = null;
    CommandLineParser parser = new BasicParser();

    options.addOption("h", "help", false, Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_HELP")); //$NON-NLS-1$

    options.addOption(OptionBuilder.withLongOpt("indexStat").withArgName("index status file path").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_INDEX_STAT")) //$NON-NLS-1$
        .create("i"));

    options.addOption(OptionBuilder.withLongOpt("queryGeneratorsStr").withArgName("query generators").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_QUERY_GENERATORS")) //$NON-NLS-1$
        .create("q"));

    options.addOption(OptionBuilder.withLongOpt("indexDir").withArgName("index file directory path").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_INDEX_DIR")) //$NON-NLS-1$
        .create("d"));

    options.addOption(OptionBuilder.withLongOpt("tsvDir").withArgName("duplicate thread file directory path").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_TSV_DIR")) //$NON-NLS-1$
        .create("v"));

    options.addOption(OptionBuilder.withLongOpt("uniqDir").withArgName("unique thread file directory path").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_UNIQUE_DIR")) //$NON-NLS-1$
        .create("u"));

    options.addOption(OptionBuilder.withLongOpt("resDir")
        .withArgName(
            "The directory path for by products generated by the application(e.g. index file, stats, csv file for dupthreads)")
        .hasArg().withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_RES_DIR")) //$NON-NLS-1$
        .create("r"));

    options.addOption(OptionBuilder.withLongOpt("caAnswerNum")
        .withArgName("The number of candidate answers returned by the candidate answer generator").hasArg()
        .withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_CANDIDATE_ANSWER_NUM")) //$NON-NLS-1$
        .create("b"));

    options.addOption(OptionBuilder.isRequired().withLongOpt("configure").withArgName("configuration file name")
        .hasArg().withDescription(Messages.getString("RetrieveAndRank.CLI_DESCRIPTION_CONFIGURE")) //$NON-NLS-1$
        .create("c"));
    try {
      cmdLine = parser.parse(options, args);
    } catch (ParseException e) {
      throw new IngestionException(e);
    }

    return cmdLine;
  }

  private static void generate_query_questions() throws SearchException {
    final String[] questionTitle = {"What good robotics software platforms / operating systems are available?"};
    if (queryQuestions == null)
      queryQuestions = new StackExchangeQuestion[] {new StackExchangeQuestion(questionTitle[0], "", null, 1, "")};
  }

  private static void generate_queries_and_find_candidate_answers() throws SearchException {
    answerGenerator = new StackExchangeThreadAnswerGenerator();
    answerGenerator.initialize(appProperties);
    for (StackExchangeQuestion question : queryQuestions) {
      Observable<CandidateAnswer> answers = answerGenerator.generateCandidateAnswers(question);
      List<String> answerTitles = new ArrayList<String>();
      for (CandidateAnswer answer : answers.toList().toBlocking().single()) {
        StackExchangeThread thread = (StackExchangeThread) answer;
        answerTitles.add(
            thread.getQuestion().getTitle() + "[RANK]=" + answer.getFeatureValue(PipelineConstants.FEATURE_SEARCH_RANK)
                + " [SEARCH_SCORE]=" + answer.getFeatureValue(PipelineConstants.FEATURE_SEARCH_SCORE));
      }
      questionAnswerMap.put(question.getTitleText(), answerTitles);
    }
  }

  private static void print_out_answers_retrived_from_query() {
    for (String queryTitle : questionAnswerMap.keySet()) {
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.QUESTION_TITLE"), queryTitle)); //$NON-NLS-1$
      List<String> answerList = questionAnswerMap.get(queryTitle);
      logger.info("{");
      for (String answer : answerList)
        logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.ANSWER"), answer)); //$NON-NLS-1$ );
      logger.info("}");
      logger.info(MessageFormat.format(Messages.getString("RetrieveAndRank.ANSWER_NUM"), answerList.size())); //$NON-NLS-1$
    }
  }

  private static void clear_res_directory() {
    String dupThread = appProperties.getProperty(ConfigurationConstants.DUPLICATE_THREAD_DIR);
    String uniqueThread = appProperties.getProperty(ConfigurationConstants.UNIQUE_THREAD_SER_PATH);
    String searchIndex = appProperties.getProperty(ConfigurationConstants.INDEX_DIR);
    try {
      FileUtils.deleteDirectory(new File(dupThread));
    } catch (IOException e) {
    }
    try {
      FileUtils.deleteDirectory(new File(uniqueThread));
    } catch (IOException e) {
    }
    try {
      FileUtils.deleteDirectory(new File(searchIndex));
    } catch (IOException e) {
    }

  }
}
