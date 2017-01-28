package com.yoloo.android.util;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;

public final class ControllerUtil {

  private ControllerUtil() {
  }

  public static Class<? extends Controller> getPreviousControllerClass(Controller current) {
    final Router router = current.getRouter();
    return router.getBackstack()
        .get(router.getBackstackSize() - 2)
        .controller()
        .getClass();
  }
}
