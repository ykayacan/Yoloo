package com.yoloo.android;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.io.File;
import timber.log.Timber;

public class YolooApp extends Application {

  private static YolooApp currentApplication = null;

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  //private RefWatcher refWatcher;

  public static YolooApp getInstance() {
    return currentApplication;
  }

  public static File getCacheDirectory() {
    return currentApplication.getCacheDir();
  }

  /*public static RefWatcher getRefWatcher(Context context) {
    YolooApp application = (YolooApp) context.getApplicationContext();
    return application.refWatcher;
  }*/

  @Override
  public void onCreate() {
    super.onCreate();

    initializeTimber();
    //initializeLeakCanary();
    initializeRealm();
    //enabledStrictMode();
    Stetho.initialize(
        Stetho.newInitializerBuilder(this)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
            .build());

    /*TinyDancer.create()
        .show(this);*/

    currentApplication = this;
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  private void initializeFacebook() {
    FacebookSdk.sdkInitialize(this);
    AppEventsLogger.activateApp(this);
  }

  private void initializeRealm() {
    Realm.init(this);
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
        .deleteRealmIfMigrationNeeded()
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);
  }

  /*private void initializeLeakCanary() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    refWatcher = LeakCanary.install(this);
  }*/

  private void initializeTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree() {
        // Add the line number to the tag
        @Override
        protected String createStackElementTag(StackTraceElement element) {
          return super.createStackElementTag(element) + ":" + element.getLineNumber();
        }
      });
    } else {
      Timber.plant(new ReleaseTree());
    }
  }

  private void enabledStrictMode() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .penaltyDeath()
          .build());
    }
  }

  /**
   * A tree which logs important information for crash reporting.
   */
  private static class ReleaseTree extends Timber.Tree {

    private static final int MAX_LOG_LENGTH = 4000;

    @Override
    protected boolean isLoggable(String tag, int priority) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return false;
      }

      // Only log WARN, INFO, ERROR, WTF
      return true;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
      if (isLoggable(tag, priority)) {
        // Message is short enough, does not need to be broken into chunks
        if (message.length() < MAX_LOG_LENGTH) {
          if (priority == Log.ASSERT) {
            Log.wtf(tag, message);
          } else {
            Log.println(priority, tag, message);
          }
          return;
        }

        // Split by line, then ensure each line can fit into Log's maximum length
        for (int i = 0, length = message.length(); i < length; i++) {
          int newline = message.indexOf('\n', i);
          newline = newline != -1 ? newline : length;
          do {
            int end = Math.min(newline, i + MAX_LOG_LENGTH);
            String part = message.substring(i, end);
            if (priority == Log.ASSERT) {
              Log.wtf(tag, part);
            } else {
              Log.println(priority, tag, part);
            }
            i = end;
          } while (i < newline);
        }
      }
    }
  }
}