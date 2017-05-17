package com.yoloo.android.data.repository.post;

import android.support.annotation.NonNull;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TrendingBlogPostsUpdateJob extends Job {

  public static final String TAG = "job.update.trendingblogposts";

  public static void scheduleJob() {
    scheduleJob(true);
  }

  public static void scheduleJob(boolean updateCurrent) {
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);

    // 1 AM - 2 AM, ignore seconds
    long startMs = TimeUnit.MINUTES.toMillis(60 - minute)
        + TimeUnit.HOURS.toMillis((24 - hour) % 24);
    long endMs = startMs + TimeUnit.HOURS.toMillis(1);

    new JobRequest.Builder(TAG)
        .setExecutionWindow(startMs, endMs)
        .setPersisted(true)
        .setUpdateCurrent(updateCurrent)
        .build()
        .schedule();
  }

  @NonNull @Override protected Result onRunJob(Params params) {
    try {
      PostRemoteDataStore.getInstance().listByTrendingBlogPosts(null, 7)
          .doOnNext(
              response -> PostDiskDataStore.getInstance().addTrendingBlogs(response.getData()))
          .blockingSingle();
      return Result.SUCCESS;
    } finally {
      scheduleJob(false);
    }
  }
}
