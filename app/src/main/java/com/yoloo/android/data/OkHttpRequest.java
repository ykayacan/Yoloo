package com.yoloo.android.data;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.yoloo.android.util.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class OkHttpRequest extends LowLevelHttpRequest {

  private final HttpURLConnection connection;

  public OkHttpRequest(HttpURLConnection connection) {
    this.connection = connection;
    connection.setInstanceFollowRedirects(false);
  }

  @Override public void addHeader(String name, String value) throws IOException {
    connection.addRequestProperty(name, value);
  }

  @Override public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
    connection.setReadTimeout(readTimeout);
    connection.setConnectTimeout(connectTimeout);
  }

  @Override public LowLevelHttpResponse execute() throws IOException {
    HttpURLConnection connection = this.connection;
    // write content
    if (getStreamingContent() != null) {
      String contentType = getContentType();
      if (contentType != null) {
        addHeader("Content-Type", contentType);
      }
      String contentEncoding = getContentEncoding();
      if (contentEncoding != null) {
        addHeader("Content-Encoding", contentEncoding);
      }
      long contentLength = getContentLength();
      if (contentLength >= 0) {
        addHeader("Content-Length", Long.toString(contentLength));
      }
      String requestMethod = connection.getRequestMethod();
      if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
        connection.setDoOutput(true);
        // see http://developer.android.com/reference/java/net/HttpURLConnection.html
        if (contentLength >= 0 && contentLength <= Integer.MAX_VALUE) {
          connection.setFixedLengthStreamingMode((int) contentLength);
        } else {
          connection.setChunkedStreamingMode(0);
        }
        OutputStream out = connection.getOutputStream();
        try {
          getStreamingContent().writeTo(out);
        } finally {
          out.close();
        }
      } else {
        // cannot call setDoOutput(true) because it would change a GET method to POST
        // for HEAD, OPTIONS, DELETE, or TRACE it would throw an exceptions
        Preconditions.checkArgument(
            contentLength == 0, "%s with non-zero content length is not supported", requestMethod);
      }
    }
    // connect
    boolean successfulConnection = false;
    try {
      connection.connect();
      OkHttpResponse response = new OkHttpResponse(connection);
      successfulConnection = true;
      return response;
    } catch (Exception e) {
      connection.connect();
      OkHttpResponse response = new OkHttpResponse(connection);
      successfulConnection = true;
      return response;
    } finally {
      if (!successfulConnection) {
        connection.disconnect();
      }
    }
  }
}
