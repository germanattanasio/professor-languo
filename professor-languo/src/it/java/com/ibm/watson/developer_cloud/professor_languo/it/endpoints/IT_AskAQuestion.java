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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.junit.Assert;
import org.junit.Test;

public class IT_AskAQuestion {

  /**
   * Tests if the ask a question endpoint can be accessed and checks if the return is in JSON
   * format.
   * 
   * @throws Exception
   */
  @Test public void askAQuestionTest() throws Exception {
    String postUrl = System.getProperty("app.url");
    postUrl = postUrl + "/api/ask_question";

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost postRequest = new HttpPost(postUrl);
    postRequest.addHeader("accept", "application/json");
    postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");

    List<NameValuePair> formParams = new ArrayList<NameValuePair>();
    formParams.add(new BasicNameValuePair("questionText", "question text"));

    postRequest.setEntity(new UrlEncodedFormEntity(formParams));

    HttpResponse response = httpClient.execute(postRequest);

    String jsonString = EntityUtils.toString(response.getEntity());

    Assert.assertEquals("response was: " + jsonString, 200, response.getStatusLine().getStatusCode());

    try {
      new JSONArray(jsonString);
    } catch (JSONException e) {
      Assert.fail("Response is not in JSON format: " + jsonString + e.getMessage());
    }
  }
}
