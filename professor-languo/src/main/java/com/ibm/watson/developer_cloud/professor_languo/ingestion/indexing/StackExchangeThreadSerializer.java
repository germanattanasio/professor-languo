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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;

import com.ibm.watson.developer_cloud.professor_languo.exception.IngestionException;
import com.ibm.watson.developer_cloud.professor_languo.model.stack_exchange.*;

/**
 * The serialization and de-serialization tool box
 * 
 */
public class StackExchangeThreadSerializer {

  /**
   * Serialize a java Object into a byte array
   * 
   * @param objToSerialize - Java Object to be serialized
   * @return a byte array serialized from the java Object
   * @throws IngestionException
   */
  public static byte[] serializeObjToBinArr(Object objToSerialize) throws IngestionException {
    ByteArrayOutputStream binOut = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(binOut);
      out.writeObject(objToSerialize);
      out.close();
      binOut.close();
    } catch (IOException e) {
      throw new IngestionException(e);
    }
    return binOut.toByteArray();
  }

  /**
   * reproduce the Java Object with the byte array
   * 
   * @param binCode - the byte array for that Java Object
   * @return the original Java Object before serialization
   * @throws IngestionException
   */
  public static Object deserializeObjFromBinArr(byte[] binCode) throws IngestionException {
    Object deserializedObj = null;
    ByteArrayInputStream binIn = new ByteArrayInputStream(binCode);
    try {
      ObjectInputStream in = new ObjectInputStream(binIn);
      deserializedObj = in.readObject();
      in.close();
      binIn.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new IngestionException(e);
    }
    return deserializedObj;
  }

  /**
   * Serialize a StackExchangeThread into a byte array
   * 
   * @param threadToSerialize - StackExchangeThread to be serialized
   * @return a byte array serialized from the StackExchangeThread
   * @throws IngestionException
   */
  public static byte[] serializeThreadToBinArr(StackExchangeThread threadToSerialize) throws IngestionException {
    ByteArrayOutputStream binOut = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(binOut);
      out.writeObject(threadToSerialize);
      out.close();
      binOut.close();
    } catch (IOException e) {
      throw new IngestionException(e);
    }
    return binOut.toByteArray();
  }

  /**
   * reproduce the StackExchangeThread with the byte array
   * 
   * @param binCode - the byte array for that StackExchangeThread
   * @return the original StackExchangeThread before serialization
   * @throws IngestionException
   */
  public static StackExchangeThread deserializeThreadFromBinArr(byte[] binCode) throws IngestionException {
    Object deserializedObj = null;
    ByteArrayInputStream binIn = new ByteArrayInputStream(binCode);
    try {
      ObjectInputStream in = new ObjectInputStream(binIn);
      deserializedObj = in.readObject();
      in.close();
      binIn.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new IngestionException(e);
    }
    return (StackExchangeThread) deserializedObj;
  }

  /**
   * Serialize a StackExchangeThread into a binary file
   * 
   * @param threadToSerialize - The StackExchangeThread to be serialized
   * @return the path(relative to the resource folder) of the serialized binary file
   * @throws IngestionException
   */
  public static String serializeThreadToBinFile(StackExchangeThread threadToSerialize, String dirPath)
      throws IngestionException {
    String binFileName = threadToSerialize.getId() + StackExchangeConstants.BIN_FILE_SUFFIX;
    try {
      String binFilePath = dirPath + binFileName;
      File serFile = new File(binFilePath);
      if (serFile.getParentFile() != null)
        serFile.getParentFile().mkdirs();
      if (!serFile.exists())
        serFile.createNewFile();
      OutputStream binOut = new FileOutputStream(serFile);
      ObjectOutputStream out = new ObjectOutputStream(binOut);
      out.writeObject(threadToSerialize);
      out.close();
      binOut.close();
    } catch (IOException e) {
      throw new IngestionException(e);
    }
    return binFileName;
  }

  public static StackExchangeThread deserializeThreadFromBinFile(String binFile) throws IngestionException {
    Object deserializedObj = null;
    try {
      InputStream binIn = new FileInputStream(binFile);
      ObjectInputStream in = new ObjectInputStream(binIn);
      deserializedObj = in.readObject();
      in.close();
      binIn.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new IngestionException(e);
    }
    return (StackExchangeThread) deserializedObj;
  }

  /**
   * reproduce the StackExchangeThread with the binary file
   * 
   * @param binFileName - the path(relative to the directory dirName) of the serialized binary file
   * @param dirName - the directory path containing the binary file
   * @return the original StackExchangeThread before serialization
   * @throws IngestionException
   */
  public static StackExchangeThread deserializeThreadFromBinFile(String binFileName, String dirName)
      throws IngestionException {
    return deserializeThreadFromBinFile(dirName + binFileName);
  }

  /**
   * Extract the major fields from the duplicate StackExchangeQuestion And save those key fields in
   * the txt File.
   * 
   * @param dupThread - the duplicate StackExchange Thread to be serialized to the txt file
   * @param originThreadId - post id of the StackExchange Thread representing the other duplicate
   *        threads
   * @param serFileName - path(relative to the csvDir folder) of the binary serialization file
   * @throws IngestionException
   */
  public static void serializeDupThreadToTsvFile(CSVPrinter csvPrinter, StackExchangeThread dupThread,
      int originThreadId, String serFileName) throws IngestionException {
    // CSVPrinter csvPrinter = getCsvPrinter(tsvDir);
    List<String> record = new ArrayList<String>(Arrays.asList(Integer.toString(dupThread.getId()),
        dupThread.getQuestion().getTitle(), dupThread.getQuestion().getBody(), Integer.toString(originThreadId),
        serFileName, dupThread.getConcatenatedTagsText()));
    try {
      csvPrinter.printRecord(record);
    } catch (IOException e) {
      throw new IngestionException(e);
    }
  }

}
