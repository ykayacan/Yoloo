package com.yoloo.android.data;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.LowLevelHttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OkHttpResponse extends LowLevelHttpResponse {

  private final HttpURLConnection connection;
  private final int responseCode;
  private final String responseMessage;
  private final ArrayList<String> headerNames = new ArrayList<>();
  private final ArrayList<String> headerValues = new ArrayList<>();

  OkHttpResponse(HttpURLConnection connection) throws IOException {
    this.connection = connection;
    int responseCode = connection.getResponseCode();
    this.responseCode = responseCode == -1 ? 0 : responseCode;
    responseMessage = connection.getResponseMessage();
    List<String> headerNames = this.headerNames;
    List<String> headerValues = this.headerValues;
    for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
      String key = entry.getKey();
      if (key != null) {
        for (String value : entry.getValue()) {
          if (value != null) {
            headerNames.add(key);
            headerValues.add(value);
          }
        }
      }
    }
  }

  @Override public InputStream getContent() throws IOException {
    HttpURLConnection connection = this.connection;
    return HttpStatusCodes.isSuccess(responseCode)
        ? connection.getInputStream() : connection.getErrorStream();
  }

  @Override public String getContentEncoding() throws IOException {
    return connection.getContentEncoding();
  }

  @Override public long getContentLength() throws IOException {
    String string = connection.getHeaderField("Content-Length");
    return string == null ? -1 : Long.parseLong(string);
  }

  @Override public String getContentType() throws IOException {
    return connection.getHeaderField("Content-Type");
  }

  @Override public String getStatusLine() throws IOException {
    String result = connection.getHeaderField(0);
    return result != null && result.startsWith("HTTP/1.") ? result : null;
  }

  @Override public int getStatusCode() throws IOException {
    return responseCode;
  }

  @Override public String getReasonPhrase() throws IOException {
    return responseMessage;
  }

  @Override public int getHeaderCount() throws IOException {
    return headerNames.size();
  }

  @Override public String getHeaderName(int index) throws IOException {
    return headerNames.get(index);
  }

  @Override public String getHeaderValue(int index) throws IOException {
    return headerValues.get(index);
  }

  @Override public void disconnect() throws IOException {
    connection.disconnect();
  }
}
