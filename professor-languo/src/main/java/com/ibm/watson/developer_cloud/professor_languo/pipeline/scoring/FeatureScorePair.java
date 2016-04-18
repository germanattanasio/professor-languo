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

/**
 * A simple implementation of a key/value pair object
 *
 */
public class FeatureScorePair {

  private final String key;
  private final Double value;

  /**
   * Constructs an instance of this object with a key and a value.
   * 
   * @param key the key to associate with the value
   * @param value the value to associate with the key
   */
  public FeatureScorePair(String key, Double value) {
    this.key = key;
    this.value = value;
  }

  /**
   * 
   * @return the key of the key/value pair object.
   */
  public String getKey() {
    return key;
  }

  /**
   * 
   * @return the value of the key/value pair object.
   */
  public Double getValue() {
    return value;
  }

}
