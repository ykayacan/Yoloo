package com.yoloo.android.feature.editor.job;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.yoloo.android.feature.editor.SendPostDelegate;

public class SendPostJobService extends JobService {

  private SendPostDelegate delegate = SendPostDelegate.create();

  @Override public boolean onStartJob(JobParameters params) {
    delegate.sendPost(getApplicationContext(), false);
    return false;
  }

  @Override public boolean onStopJob(JobParameters params) {
    delegate.onStop();
    return false;
  }
}
