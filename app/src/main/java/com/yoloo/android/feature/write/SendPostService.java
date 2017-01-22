package com.yoloo.android.feature.write;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.util.NotificationUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class SendPostService extends Service {

  public static final String KEY_NEW_POST_ID = "NEW_POST_ID";
  public static final String KEY_NEW_POST_EVENT = "NEW_POST_EVENT";

  private static final String KEY_LAST_NOTIFICATION_ID = "LAST_NOTIFICATION_ID";

  private static final int POST_NOTIFICATION_ID = 100;

  private WeakHandler handler = new WeakHandler();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {

    final int lastNotificationId = intent.getIntExtra(KEY_LAST_NOTIFICATION_ID, -1);
    if (lastNotificationId != -1) {
      NotificationUtil.cancel(lastNotificationId);
    }

    PostRepository postRepository = getPostRepository();

    postRepository.addOrGetDraft()
        .doOnSubscribe(disposable -> showSendingNotification())
        .flatMap(draft -> Single.fromObservable(postRepository.add(draft)))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(post -> {
          postRepository.deleteDraft();
          showSuccessfulNotification();
          broadcastNewPostEvent(post);
        })
        .doOnError(Timber::e)
        .subscribe();

    return START_REDELIVER_INTENT;
  }

  private PostRepository getPostRepository() {
    return PostRepository.getInstance(PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance());
  }

  private void showSendingNotification() {
    Notification.Builder builder = new Notification.Builder(SendPostService.this).setTicker(
        getString(R.string.label_sending_post))
        .setContentTitle(getString(R.string.label_sending_post))
        .setContentText("Text is sending")
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_cloud_upload_white_24dp)
        .setProgress(0, 100, true);

    Notification notification = builder.build();

    NotificationUtil.show(notification, POST_NOTIFICATION_ID);
  }

  private void showSuccessfulNotification() {
    Notification.Builder builder = new Notification.Builder(SendPostService.this).setTicker(
        getString(R.string.label_sending_post_successful))
        .setContentTitle(getString(R.string.label_sending_post_successful))
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_cloud_done_white_24dp)
        .setOngoing(false);

    Notification notification = builder.build();

    NotificationUtil.show(notification, POST_NOTIFICATION_ID);

    handler.postDelayed(() -> {
      NotificationUtil.cancel(POST_NOTIFICATION_ID);
      stopForeground(true);
      stopSelf();
    }, 3000);
  }

  public void broadcastNewPostEvent(PostRealm post) {
    Intent intent = new Intent(KEY_NEW_POST_EVENT);
    intent.putExtra(KEY_NEW_POST_ID, post.getId());

    LocalBroadcastManager.getInstance(SendPostService.this).sendBroadcast(intent);
  }
}
