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

package com.ibm.watson.developer_cloud.professor_languo.ingestion.indexing;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.util.NamedList;

import com.ibm.watson.developer_cloud.professor_languo.configuration.Messages;

/**
 * An {@link HttpSolrClient} that performs authentication to the Watson Solr Server given user
 * credentials
 *
 */
public class WatsonSolrClient extends HttpSolrClient {

  private static final long serialVersionUID = -9113623319459002463L;

  private final String userPass;

  public WatsonSolrClient(String baseURL, final String username, final String password) {
    super(baseURL);
    if (username == null || password == null) {
      throw new IllegalArgumentException(Messages.getString("RetrieveAndRank.BAD_CREDENTIAL"));
    }
    userPass = username + ":" + password;
  }

  /**
   * Include authentication info in execute method
   */
  @Override protected NamedList<Object> executeMethod(HttpRequestBase method, ResponseParser parser)
      throws SolrServerException {
    Base64 b = new Base64();
    String encoded = b.encodeAsString(new String(userPass).getBytes());
    method.setHeader(new BasicHeader("Authorization", "Basic " + encoded));
    return super.executeMethod(method, parser);
  }
}
