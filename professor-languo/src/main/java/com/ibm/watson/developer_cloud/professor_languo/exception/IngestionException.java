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

package com.ibm.watson.developer_cloud.professor_languo.exception;

/**
 * An {@link Exception} that is thrown during ingestion of a corpus
 *
 */
public class IngestionException extends ApplicationException {

  private static final long serialVersionUID = 1705570638620034039L;

  /**
   * Create a new {@link IngestionException} with a given message
   * 
   * @param msg - The exception message
   */
  public IngestionException(String msg) {
    super(msg);
  }

  /**
   * Create a new {@link IngestionException} with a given cause
   * 
   * @param cause - The {@link Throwable} cause of the exception
   */
  public IngestionException(Throwable cause) {
    super(cause);
  }

}
