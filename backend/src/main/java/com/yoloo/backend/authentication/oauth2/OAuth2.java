package com.yoloo.backend.authentication.oauth2;

public final class OAuth2 {
  public static final String OAUTH_RESPONSE_TYPE = "response_type";
  public static final String OAUTH_CLIENT_ID = "client_id";
  public static final String OAUTH_CLIENT_SECRET = "client_secret";
  public static final String OAUTH_REDIRECT_URI = "redirect_uri";
  public static final String OAUTH_USERNAME = "username";
  public static final String OAUTH_PASSWORD = "password";
  public static final String OAUTH_ASSERTION_TYPE = "assertion_type";
  public static final String OAUTH_ASSERTION = "assertion";
  public static final String OAUTH_SCOPE = "scope";
  public static final String OAUTH_STATE = "state";
  public static final String OAUTH_GRANT_TYPE = "grant_type";
  public static final String OAUTH_HEADER_NAME = "Bearer";
  public static final String OAUTH_CODE = "code";
  public static final String OAUTH_ACCESS_TOKEN = "access_token";
  public static final String OAUTH_EXPIRES_IN = "expires_in";
  public static final String OAUTH_REFRESH_TOKEN = "refresh_token";
  public static final String OAUTH_TOKEN_TYPE = "token_type";
  public static final String OAUTH_TOKEN = "oauth_token";
  public static final String OAUTH_TOKEN_DRAFT_0 = "access_token";
  public static final String OAUTH_BEARER_TOKEN = "access_token";
  public static final String OAUTH_VERSION_DIFFER = "oauth_signature_method";
  public static final String ASSERTION = "assertion";

  private OAuth2() {
  }

  public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"), PASSWORD("password"), REFRESH_TOKEN("refresh_token");

    private String grantType;

    GrantType(String grantType) {
      this.grantType = grantType;
    }

    @Override public String toString() {
      return grantType;
    }
  }

  public static final class ContentType {
    public static final String URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String JSON = "application/json";

    public ContentType() {
    }
  }

  public static final class HeaderType {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String AUTHORIZATION = "Authorization";
    public static final String PROVIDER = "X-Provider";

    public HeaderType() {
    }
  }
}
