package com.ibm.watson.developer_cloud.professor_languo.endpoints;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.utils.ProviderUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;

@Provider
@Produces(MediaType.APPLICATION_JSON)
/**
 * Provider to write the wink.json4j.JSONArray type to html message body
 *
 */
public class WinkProvider implements MessageBodyWriter<JSONArray> {

  public long getSize(JSONArray obj, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
    // always return -1 because the size cannot be determined in advance
    return -1;
  }

  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
    // return true if clazz is in JSONArray family
    return JSONArray.class.isAssignableFrom(clazz);
  }

  public void writeTo(JSONArray arr, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> headers, OutputStream os) throws IOException, WebApplicationException {

    mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(headers, mediaType);
    OutputStreamWriter writer = new OutputStreamWriter(os, ProviderUtils.getCharset(mediaType));
    try {
      Writer json4jWriter = arr.write(writer);
      json4jWriter.flush();
      writer.flush();
    } catch (JSONException e) {
      throw new WebApplicationException(e);
    }
  }
}
