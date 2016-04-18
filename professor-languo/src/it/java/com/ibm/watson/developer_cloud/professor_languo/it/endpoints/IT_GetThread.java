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

package com.ibm.watson.developer_cloud.professor_languo.it.endpoints;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integrated tests for the live GetThreadResource enpoint
 */
public class IT_GetThread {
  private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
  private final String GET_URL = System.getProperty("app.url") + "/api/thread/";

  /**
   * Test what happens when a properly formated id request is sent to endpoint
   * 
   * @throws Exception
   */
  @Test public void getThreadWithInteger() throws Exception {

    // Set up the connection
    String id = String.valueOf(Integer.MAX_VALUE);
    String getUrl = GET_URL + id;

    // Execute the query and get the response
    HttpResponse response = httpClient.execute(new HttpGet(getUrl));
    String jsonString = EntityUtils.toString(response.getEntity());

    // The response has to be a 200 if the id is found or 404 if it is not
    Assert.assertTrue("response was: " + jsonString,
        response.getStatusLine().getStatusCode() == 404 || response.getStatusLine().getStatusCode() == 200);
  }

  /**
   * Test what a happens when a get is sent with a poorly formated id
   * 
   * @throws Exception
   */
  @Test public void getThreadWithBadInt() throws Exception {

    // Setup the connection

    String id = "not_int";
    String getUrl = GET_URL + id;

    // Execute query and get response
    HttpResponse response = httpClient.execute(new HttpGet(getUrl));
    String jsonString = EntityUtils.toString(response.getEntity());

    // The response to a malformated id must be a 500 error
    Assert.assertEquals("response was: " + jsonString, 500, response.getStatusLine().getStatusCode());
  }
}
