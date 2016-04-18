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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.content.FileBody;

/**
 * A FileBody used to send the answer data in csv format via post request
 */
public class AnswerFileBody extends FileBody {

  private byte[] bytes;

  public AnswerFileBody(String answerData) {
    super(new File("."));
    bytes = answerData.getBytes();
  }

  @Override public void writeTo(OutputStream out) throws IOException {
    out.write(bytes);
  }

  @Override public long getContentLength() {
    return bytes.length;
  }

}
