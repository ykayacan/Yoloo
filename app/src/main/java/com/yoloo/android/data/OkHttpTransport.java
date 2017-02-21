package com.yoloo.android.data;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.OkHttpClient;

public class OkHttpTransport extends HttpTransport {

  /**
   * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
   * ascending alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS = {
      HttpMethods.DELETE,
      HttpMethods.GET,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.POST,
      HttpMethods.PUT,
      HttpMethods.TRACE
  };

  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  /** Factory to produce connections from {@link URL}s */
  //private final ConnectionFactory connectionFactory;

  /** SSL socket factory or {@code null} for the default. */
  //private final SSLSocketFactory sslSocketFactory;

  /** Host name verifier or {@code null} for the default. */
  //private final HostnameVerifier hostnameVerifier;

  private OkHttpClient client;

  public OkHttpTransport(OkHttpClient client, SSLSocketFactory sslSocketFactory,
      HostnameVerifier hostnameVerifier) {
    if (client != null) {
      this.client = client;
    } else {
      this.client = new OkHttpClient();
    }
  }

  @Override public boolean supportsMethod(String method) throws IOException {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override protected LowLevelHttpRequest buildRequest(String method, String url)
      throws IOException {
    /*Preconditions.checkArgument(supportsMethod(method),
        "HTTP method %s not supported", method);
    // connection with proxy settings
    URL connUrl = new URL(url);
    if (proxy != null) {
      okClient.setProxy(proxy);
    }
    new OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory)
        .hostnameVerifier(hostnameVerifier);
    URLConnection conn = client.open(connUrl);
    HttpURLConnection connection = (HttpURLConnection) conn;
    connection.setRequestMethod(method);
    // SSL settings
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection secureConnection = (HttpsURLConnection) connection;

      if (hostnameVerifier != null) {
        secureConnection.setHostnameVerifier(hostnameVerifier);
      }
      if (sslSocketFactory != null) {
        secureConnection.setSSLSocketFactory(sslSocketFactory);
      }
    }*/
    return null;
  }
}
