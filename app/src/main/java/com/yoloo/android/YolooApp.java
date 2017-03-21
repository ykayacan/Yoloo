package com.yoloo.android;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.faker.PostFaker;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class YolooApp extends Application {

  private static Context appContext;

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  public static File getCacheDirectory() {
    return appContext.getCacheDir();
  }

  public static Context getAppContext() {
    return appContext;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    appContext = this;

    initTimber();
    initRealm();
    initStetho();
    FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    initCalligraphy();

    AccountFaker.generateAll();
    PostFaker.fakePosts();

    //enabledStrictMode();
    //TinyDancer.create().show(this);
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  private void initCalligraphy() {
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build());
  }

  private void initStetho() {
    Stetho.initialize(
        Stetho.newInitializerBuilder(this)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
            .build());
  }

  private void initRealm() {
    Realm.init(this);
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
        .deleteRealmIfMigrationNeeded()
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);
  }

  private void enabledStrictMode() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .penaltyDeath()
          .build());

      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build());
    }
  }

  private void initTimber() {
    Timber.plant(BuildConfig.DEBUG
        ? new Timber.DebugTree()
        : new ReleaseTree());
  }

  private static class ReleaseTree extends Timber.Tree {

    @Override
    protected boolean isLoggable(String tag, int priority) {
      return !(priority == Log.VERBOSE || priority == Log.DEBUG);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
      if (isLoggable(tag, priority)) {
        Throwable t = throwable != null
            ? throwable
            : new Exception(message);

        // Firebase Crash Reporting
        FirebaseCrash.logcat(priority, tag, message);
        FirebaseCrash.report(t);
      }
    }
  }
}
