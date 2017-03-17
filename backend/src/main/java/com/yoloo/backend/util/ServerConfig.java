package com.yoloo.backend.util;

import com.google.appengine.api.utils.SystemProperty;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ServerConfig {

  public static boolean isDev() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }

  public static boolean isTest() {
    return SystemProperty.environment.value() == null;
  }
}
