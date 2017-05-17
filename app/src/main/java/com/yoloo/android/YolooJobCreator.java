package com.yoloo.android;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.yoloo.android.data.repository.group.GroupUpdateJob;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.feature.editor.job.SendPostJob;

public final class YolooJobCreator implements JobCreator {
  @Override
  public Job create(String tag) {
    switch (tag) {
      case SendPostJob.TAG:
        return new SendPostJob(PostRepositoryProvider.getRepository());
      case GroupUpdateJob.TAG:
        return new GroupUpdateJob();
      default:
        return null;
    }
  }
}
