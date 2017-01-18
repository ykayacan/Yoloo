package com.yoloo.backend.util;

import com.google.appengine.api.utils.SystemProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ServerConfig {

  public static boolean isDev() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }
}
