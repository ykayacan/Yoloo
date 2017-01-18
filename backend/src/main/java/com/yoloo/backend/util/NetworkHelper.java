package com.yoloo.backend.util;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import java.net.URL;

public enum NetworkHelper {
  INSTANCE;

  private static HTTPRequest request;

  public HTTPRequest getRequest(URL url, HTTPMethod method) {
    if (request == null) {
      request = new HTTPRequest(url, method);
    }
    return request;
  }
}
