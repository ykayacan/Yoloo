package com.yoloo.android;

import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import timber.log.Timber;

class ReleaseTree extends Timber.Tree {

  @Override protected boolean isLoggable(String tag, int priority) {
    return !(priority == Log.VERBOSE || priority == Log.DEBUG);
  }

  @Override protected void log(int priority, String tag, String message, Throwable throwable) {
    if (isLoggable(tag, priority)) {
      Throwable t = throwable != null ? throwable : new Exception(message);

      // Firebase Crash Reporting
      FirebaseCrash.logcat(priority, tag, message);
      FirebaseCrash.report(t);
    }
  }
}
