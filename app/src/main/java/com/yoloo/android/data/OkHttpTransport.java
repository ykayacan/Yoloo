package com.yoloo.android.data;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.util.Beta;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import com.yoloo.android.util.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 * <p/>
 * <p>
 * Users should consider modifying the keep alive property on {@link OkHttpTransport} to control
 * whether the socket should be returned to a pool from connected sockets. More information is
 * available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 * </p>
 * <p/>
 * <p>
 * We honor the default global caching behavior. To change the default behavior use
 * {@link HttpURLConnection#setDefaultUseCaches(boolean)}.
 * </p>
 * <p/>
 * <p>
 * Upgrade warning: in prior version 1.14 caching was always disabled, but starting with version
 * 1.15 we honor the default global caching behavior.
 * </p>
 * <p/>
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance from the HTTP transport.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.0
 */
public final class OkHttpTransport extends HttpTransport {

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

  /**
   * SSL socket factory or {@code null} for the default.
   */
  private final SSLSocketFactory sslSocketFactory;

  /**
   * Host name verifier or {@code null} for the default.
   */
  private final HostnameVerifier hostnameVerifier;

  private final OkHttpClient okHttpClient;

  /**
   * Constructor with the default behavior.
   * <p/>
   * <p>
   * Instead use {@link Builder} to modify behavior.
   * </p>
   */
  public OkHttpTransport() {
    this(null, null);
  }

  /**
   * @param sslSocketFactory SSL socket factory or {@code null} for the default
   * @param hostnameVerifier host name verifier or {@code null} for the default
   */
  private OkHttpTransport(SSLSocketFactory sslSocketFactory,
      HostnameVerifier hostnameVerifier) {
    this.sslSocketFactory = sslSocketFactory;
    this.hostnameVerifier = hostnameVerifier;
    this.okHttpClient = new OkHttpClient();
  }

  @Override public boolean supportsMethod(String method) throws IOException {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override protected LowLevelHttpRequest buildRequest(String method, String url)
      throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    // connection with proxy settings
    URL connUrl = new URL(url);

    /*if (proxy != null) {
      okHttpClient.setProxy(proxy);
    }*/
    HttpURLConnection connection = new OkUrlFactory(okHttpClient).open(connUrl);

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
    }
    return new OkHttpRequest(connection);
  }

  /**
   * Builder for {@link OkHttpTransport}.
   * <p/>
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.13
   */
  public static final class Builder {

    /**
     * SSL socket factory or {@code null} for the default.
     */
    private SSLSocketFactory sslSocketFactory;

    /**
     * Host name verifier or {@code null} for the default.
     */
    private HostnameVerifier hostnameVerifier;

    /**
     * HTTP proxy or {@code null} to use the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     */
    private Proxy proxy;

    /**
     * Sets the HTTP proxy or {@code null} to use the proxy settings from <a
     * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
     * properties</a>.
     * <p/>
     * <p>
     * For example:
     * </p>
     * <p/>
     * <pre>
     * setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
     * </pre>
     */
    public Builder setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }

    /**
     * Sets the SSL socket factory based on root certificates in a Java KeyStore.
     * <p/>
     * <p>
     * Example usage:
     * </p>
     * <p/>
     * <pre>
     * trustCertificatesFromJavaKeyStore(new FileInputStream("certs.jks"), "password");
     * </pre>
     *
     * @param keyStoreStream input stream to the key store (closed at the end from this method in a
     *                       finally block)
     * @param storePass      password protecting the key store file
     * @since 1.14
     */
    public Builder trustCertificatesFromJavaKeyStore(InputStream keyStoreStream, String storePass)
        throws GeneralSecurityException, IOException {
      KeyStore trustStore = SecurityUtils.getJavaKeyStore();
      SecurityUtils.loadKeyStore(trustStore, keyStoreStream, storePass);
      return trustCertificates(trustStore);
    }

    /**
     * Sets the SSL socket factory based root certificates generated from the specified stream using
     * {@link CertificateFactory#generateCertificates(InputStream)}.
     * <p/>
     * <p>
     * Example usage:
     * </p>
     * <p/>
     * <pre>
     * trustCertificatesFromStream(new FileInputStream("certs.pem"));
     * </pre>
     *
     * @param certificateStream certificate stream
     * @since 1.14
     */
    public Builder trustCertificatesFromStream(InputStream certificateStream)
        throws GeneralSecurityException, IOException {
      KeyStore trustStore = SecurityUtils.getJavaKeyStore();
      trustStore.load(null, null);
      SecurityUtils.loadKeyStoreFromCertificates(
          trustStore, SecurityUtils.getX509CertificateFactory(), certificateStream);
      return trustCertificates(trustStore);
    }

    /**
     * Sets the SSL socket factory based on a root certificate trust store.
     *
     * @param trustStore certificate trust store (use for example {@link SecurityUtils#loadKeyStore}
     *                   or {@link SecurityUtils#loadKeyStoreFromCertificates})
     * @since 1.14
     */
    public Builder trustCertificates(KeyStore trustStore) throws GeneralSecurityException {
      SSLContext sslContext = SslUtils.getTlsSslContext();
      SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
      return setSslSocketFactory(sslContext.getSocketFactory());
    }

    /**
     * {@link Beta} <br/>
     * Disables validating server SSL certificates by setting the SSL socket factory using
     * {@link SslUtils#trustAllSSLContext()} for the SSL context and
     * {@link SslUtils#trustAllHostnameVerifier()} for the host name verifier.
     * <p/>
     * <p>
     * Be careful! Disabling certificate validation is dangerous and should only be done in testing
     * environments.
     * </p>
     */
    @Beta
    public Builder doNotValidateCertificate() throws GeneralSecurityException {
      hostnameVerifier = SslUtils.trustAllHostnameVerifier();
      sslSocketFactory = SslUtils.trustAllSSLContext().getSocketFactory();
      return this;
    }

    /**
     * Returns the SSL socket factory.
     */
    public SSLSocketFactory getSslSocketFactory() {
      return sslSocketFactory;
    }

    /**
     * Sets the SSL socket factory or {@code null} for the default.
     */
    public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = sslSocketFactory;
      return this;
    }

    /**
     * Returns the host name verifier or {@code null} for the default.
     */
    public HostnameVerifier getHostnameVerifier() {
      return hostnameVerifier;
    }

    /**
     * Sets the host name verifier or {@code null} for the default.
     */
    public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = hostnameVerifier;
      return this;
    }

    /**
     * Returns a new instance from {@link OkHttpTransport} based on the options.
     */
    public OkHttpTransport build() {
      return new OkHttpTransport(sslSocketFactory, hostnameVerifier);
    }
  }
}
