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

package com.ibm.watson.developer_cloud.professor_languo.endpoints;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.watson.developer_cloud.professor_languo.configuration.IndexerAndSearcherFactory;
import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;
import com.ibm.watson.developer_cloud.professor_languo.data_model.CandidateAnswer;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.StackExchangeConstants.IndexDocumentFieldName;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcher;
import com.ibm.watson.developer_cloud.professor_languo.pipeline.primary_search.RetrieveAndRankSearcherConstants;

/**
 * An endpoint to return the full HTML doc of a post given the post id. The doc is retrieved via
 * querying the solr cluster by id using the /select handler
 */
@Path("/thread/{thread_post_id}")
public class GetThreadResource {
  private static Logger logger = LogManager.getLogger(GetThreadResource.class);

  // Use the /select handler to find doc with the given thread post ID
  RetrieveAndRankSearcher searcher =
      new RetrieveAndRankSearcher(RetrieveAndRankSearcherConstants.SELECT_REQUEST_HANDLER);

  /**
   * Get the thread post id as a path parameter and query the solr cluster for post html
   * 
   * @param threadPostID the id as an integer of the thread post to lookup
   * @return the doc html if the doc with the given id is found or a 404 error msg if the doc is not
   *         found.
   * @throws Exception
   */
  @GET @Produces(MediaType.TEXT_HTML) public Response getThread(@PathParam("thread_post_id") String threadPostID)
      throws Exception {

    // Parse the thread post to an integer
    threadPostID.replaceAll("\\D", "");
    threadPostID = String.valueOf(Integer.parseInt(threadPostID));

    Properties appProperties = new Properties();
    appProperties.load(this.getClass().getResourceAsStream("/app_config.properties"));

    // loading configs from the server.env file
    try {
      IndexerAndSearcherFactory.loadStaticBluemixProperties(appProperties);
    } catch (IOException | IllegalArgumentException e) {
      logger.error(e);
    }
    logger.debug(appProperties.toString());

    // initialize the searcher
    searcher.initialize(appProperties);

    Collection<CandidateAnswer> answers =
        searcher.performSearch(IndexDocumentFieldName.THREAD_POST_ID.toString() + ":" + threadPostID);

    // if no thread with the ID is found return a not found error
    if (answers.size() == 0) {
      final String msg = MessageFormat.format(Messages.getString("RetrieveAndRank.THREAD_ID_NOT_FOUND"), //$NON-NLS-1$
          threadPostID);
      logger.error(msg);
      return Response.status(404).entity(msg).build();
    }

    // If more than one answer is found then the thread post id was not
    // unique.
    // This is not supposed to happen and signals a problem with ingestion
    if (answers.size() > 1) {
      final String msg = MessageFormat.format(Messages.getString("RetrieveAndRank.THREAD_ID_NOT_UNIQUE"), //$NON-NLS-1$
          threadPostID);
      logger.error(msg);
      return Response.status(500).entity(msg).build();
    }

    CandidateAnswer answer = answers.iterator().next();
    final String answerHTML = "<!DOCTYPE html>" + answer.getAnswerDocumentText().getText().toString();
    return Response.ok(answerHTML).header("Content-Type", MediaType.TEXT_HTML + ";charset=utf-8").build();
  }
}
