package com.yoloo.android.feature.write;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.localmesaagemanager.LocalMessageManager;
import com.yoloo.android.util.NotificationUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.UUID;
import timber.log.Timber;

public class CreatePostService extends Service {

  public static final String KEY_NEW_POST_ID = "NEW_POST_ID";
  public static final String KEY_NEW_POST_EVENT = "NEW_POST_EVENT";

  private static final String KEY_LAST_NOTIFICATION_ID = "LAST_NOTIFICATION_ID";

  private static final int POST_NOTIFICATION_ID = 100;

  private WeakHandler handler = new WeakHandler();

  private Disposable disposable;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {

    final int lastNotificationId = intent.getIntExtra(KEY_LAST_NOTIFICATION_ID, -1);
    if (lastNotificationId != -1) {
      NotificationUtil.cancel(lastNotificationId);
    }

    PostRepository postRepository = getPostRepository();

    disposable = postRepository.getDraft()
        .doOnSubscribe(disposable -> showSendingNotification())
        .map(draft -> draft.setId(UUID.randomUUID().toString()).setFeedItem(true).setPending(false))
        .doOnNext(postRepository::addPost)
        /*.flatMap(postRepository::addPost)*/
        .doOnNext(postRealm -> Timber.d("Process: %s", postRealm.toString()))
        .doOnComplete(() -> postRepository.deleteDraft().subscribe())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(this::broadcastNewPostEvent)
        .doOnError(Throwable::printStackTrace)
        .doOnComplete(this::showSuccessfulNotification)
        .subscribe();

    return START_REDELIVER_INTENT;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    disposable.dispose();
  }

  private PostRepository getPostRepository() {
    return PostRepository.getInstance(
        PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance());
  }

  private void showSendingNotification() {
    Notification.Builder builder = new Notification.Builder(CreatePostService.this).setTicker(
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
    Notification.Builder builder = new Notification.Builder(CreatePostService.this).setTicker(
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
    }, 2000);
  }

  public void broadcastNewPostEvent(PostRealm post) {
    Timber.d("Broad");
    LocalMessageManager.getInstance().send(R.integer.message_create_new_post, post);
  }
}
