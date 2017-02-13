package com.yoloo.android.util;

import android.view.KeyEvent;
import android.view.View;
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

  public static void preventDefaultBackPressAction(View v, Callback callback) {
    v.setFocusableInTouchMode(true);
    v.requestFocus();
    v.setOnKeyListener((view, keyCode, event) -> {
      if (event.getAction() != KeyEvent.ACTION_DOWN) {
        return true;
      }

      if (keyCode == KeyEvent.KEYCODE_BACK) {
        callback.run();
      }

      return true;
    });
  }

  public interface Callback {
    void run();
  }
}
