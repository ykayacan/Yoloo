package com.yoloo.android.feature.editor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SendPostService extends Service {

  private SendPostDelegate delegate = SendPostDelegate.create();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    delegate.sendPost(getApplicationContext(), true);
    return START_REDELIVER_INTENT;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    delegate.onStop();
  }
}
