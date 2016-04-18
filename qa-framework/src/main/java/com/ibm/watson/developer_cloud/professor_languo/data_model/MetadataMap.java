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

package com.ibm.watson.developer_cloud.professor_languo.data_model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store metadata associated with questions/answers/documents in the form of a
 * map of key/value pairs.
 * <p>
 * This class supports thread-safe, concurrent retrieval and update of key/value pairs. Note though
 * that retrieval operations do not block on update operations, (see {@link ConcurrentHashMap} for
 * further discussion of this).
 * 
 */
public class MetadataMap implements Serializable {

  private static final long serialVersionUID = -4896194523747569485L;
  protected ConcurrentHashMap<String, Object> metadataMap;

  /**
   * Create a new {@link MetadataMap} object with an empty map of key/value pairs
   */
  public MetadataMap() {
    super();
    this.metadataMap = new ConcurrentHashMap<>();
  }

  /**
   * Create a new {@link MetadataMap} object with a shallow copy of the key/value pairs contained in
   * the provided initial collection
   * 
   * @param initialCollection - A {@link Map} of {@literal <String,Object>} key/value pairs to store
   */
  public MetadataMap(Map<String, Object> initialCollection) {
    super();
    this.metadataMap = new ConcurrentHashMap<>(initialCollection);
  }

  /**
   * Tests if the specified metadata key is in the map
   *
   * @param key - The metadata key
   * @return {@code true} if the map contains the key, {@code false} otherwise
   */
  public boolean containsKey(String key) {
    return this.metadataMap.containsKey(key);
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if this map contains no
   * mapping for the key.
   * 
   * @param key - The metadata key
   * @return The value to which the specified key is mapped, or {@code null} if this map contains no
   *         mapping for the key.
   */
  public Object get(String key) {
    return this.metadataMap.get(key);
  }

  /**
   * Returns the {@code String} value to which the specified key is mapped, or {@code null} if this
   * map contains no mapping for the key.
   * 
   * @param key - The metadata key
   * @return The {@code String} value to which the specified key is mapped, or {@code null} if this
   *         map contains no mapping for the key.
   */
  public String getValueAsString(String key) {
    return (String) this.metadataMap.get(key);
  }

  /**
   * Returns a {@code List<String>} value to which the specified key is mapped, or {@code null} if
   * this map contains no mapping for the key.
   * 
   * @param key - The metadata key
   * @return The {@code List<String>} value to which the specified key is mapped, or {@code null} if
   *         this map contains no mapping for the key.
   */
  @SuppressWarnings("unchecked") public List<String> getValueAsStringList(String key) {
    return (List<String>) this.metadataMap.get(key);
  }

  /**
   * Returns the {@code int} value to which the specified key is mapped, or {@code null} if this map
   * contains no mapping for the key.
   * 
   * @param key - The metadata key
   * @return The {@code int} value to which the specified key is mapped, or {@code null} if this map
   *         contains no mapping for the key.
   */
  public int getValueAsInteger(String key) {
    return (Integer) this.metadataMap.get(key);
  }

  /**
   * Returns the {@code double} value to which the specified key is mapped, or {@code null} if this
   * map contains no mapping for the key.
   * 
   * @param key - The metadata key
   * @return The {@code double} value to which the specified key is mapped, or {@code null} if this
   *         map contains no mapping for the key.
   */
  public double getValueAsDouble(String key) {
    return (Double) this.metadataMap.get(key);
  }

  /**
   * Returns a byte-array representation of the value to which the specified key is mapped, or
   * {@code null} if this map contains no mapping for the key.
   * 
   * @param key - The metadata key
   * @return A byte-array representation of the value to which the specified key is mapped, or
   *         {@code null} if this map contains no mapping for the key.
   */
  public byte[] getValueAsByteArray(String key) {
    return (byte[]) this.metadataMap.get(key);
  }

  /**
   * @return A {@code Set} representation of the keys contained in the metadata map
   */
  public Set<String> keySet() {
    return this.metadataMap.keySet();
  }

  /**
   * @return An entry set of {@code <String,Object>} key/value pairs contained in the metadata map
   */
  public Set<Map.Entry<String, Object>> entrySet() {
    return this.metadataMap.entrySet();
  }

  /**
   * Maps the specified key to the specified value in the metadata map. Neither the key nor the
   * value can be null.
   * 
   * @param key - String key with which the specified value is to be associated
   * @param value - (Non-null) value to be associated with the specified key
   * @return the previous value associated with key, or null if there was no mapping for key
   */
  public Object put(String key, Object value) {
    return this.metadataMap.put(key, value);
  }

  /**
   * If the specified key is not already associated with a value, associate it with the given value,
   * which must not be null.
   * 
   * @param key - String key with which the specified value is to be associated
   * @param value - (Non-null) value to be associated with the specified key
   * @return the previous value associated with key, or null if there was no mapping for key
   */
  public Object putIfAbsent(String key, Object value) {
    return this.metadataMap.putIfAbsent(key, value);
  }

  /**
   * Shallow-copies all the entries of the supplied map to the metadata map
   * 
   * @param pairs - A map of {@code <String,Object>} pairs
   */
  public void putAll(Map<String, Object> pairs) {
    this.metadataMap.putAll(pairs);
  }

  /**
   * Removes the key (and its corresponding value) from the metadata map. This method does nothing
   * if the key is not in the map.
   * 
   * @param key - The key to be removed
   * @return The value that was removed, or {@code null} if the key was not present in the metadata
   *         map
   */
  public Object remove(String key) {
    return this.metadataMap.remove(key);
  }

  /**
   * @return The number of key/value pairs presently contained in the metadata map
   */
  public int size() {
    return this.metadataMap.size();
  }
}
