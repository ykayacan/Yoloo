package com.yoloo.android.feature.editor;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.google.firebase.FirebaseException;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.feature.editor.job.SendPostJobService;
import com.yoloo.android.util.NotificationUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.UUID;
import timber.log.Timber;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SendPostDelegate {

  public static final String SEND_POST_EVENT = "SEND_POST_EVENT";
  public static final String KEY_ADD_POST = "ADD_POST";

  private static final int POST_NOTIFICATION_ID = 100;

  private WeakHandler handler = new WeakHandler();

  private Disposable disposable;

  private Context context;

  private SendPostDelegate() {
  }

  public static SendPostDelegate create() {
    return new SendPostDelegate();
  }

  public void sendPost(Context context, boolean reschedule) {
    this.context = context;

    PostRepository postRepository = getPostRepository();

    disposable = postRepository.getDraft()
        .doOnSubscribe(disposable -> showSendingNotification())
        .map(this::prepareDraft)
        .flatMapCompletable(draft -> sendPostCompletable(postRepository, draft))
        .subscribe(this::showSuccessfulNotification, throwable -> {
          if (reschedule) {
            addPostToJobQueue();
          }

          Timber.e(throwable);
          showErrorNotification(throwable);
        });
  }

  private Completable sendPostCompletable(PostRepository postRepository, PostRealm draft) {
    return Observable.just(draft)
        .flatMapSingle(postRepository::addPost)
        .doOnNext(this::broadcastNewPostEvent)
        .flatMapCompletable(post -> postRepository.deleteDraft())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private void addPostToJobQueue() {
    FirebaseJobDispatcher dispatcher =
        new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));

    Job job = dispatcher.newJobBuilder()
        .setService(SendPostJobService.class)
        .setTag("send.post.tag")
        .setLifetime(Lifetime.FOREVER)
        .setConstraints(Constraint.ON_ANY_NETWORK)
        .build();

    dispatcher.mustSchedule(job);
  }

  public void onStop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private PostRealm prepareDraft(PostRealm draft) {
    return draft.setId(UUID.randomUUID().toString())
        .setFeedItem(true)
        .setPending(false);
  }

  private PostRepository getPostRepository() {
    return PostRepository.getInstance(
        PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance()
    );
  }

  private void showSendingNotification() {
    Notification.Builder builder = new Notification.Builder(context)
        .setTicker(context.getString(R.string.label_sending_post))
        .setContentTitle(context.getString(R.string.label_sending_post))
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_cloud_upload_white_24dp)
        .setProgress(0, 100, true);

    Notification notification = builder.build();

    NotificationUtil.show(notification, POST_NOTIFICATION_ID);
  }

  private void showSuccessfulNotification() {
    Notification.Builder builder = new Notification.Builder(context)
        .setTicker(context.getString(R.string.label_sending_post_successful))
        .setContentTitle(context.getString(R.string.label_sending_post_successful))
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_cloud_done_white_24dp)
        .setOngoing(false);

    Notification notification = builder.build();

    NotificationUtil.show(notification, POST_NOTIFICATION_ID);

    handler.postDelayed(() -> NotificationUtil.cancel(POST_NOTIFICATION_ID), 1500);
  }

  private void showErrorNotification(Throwable throwable) {
    String error;

    if (throwable instanceof FirebaseException) {
      error = context.getString(R.string.error_network_unavailable);
    } else {
      error = context.getString(R.string.label_sending_post_failed);
    }

    Notification.Builder builder = new Notification.Builder(context)
        .setTicker(error)
        .setContentTitle(error)
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_error_black_24dp)
        .setOngoing(false);

    Notification notification = builder.build();

    NotificationUtil.show(notification, POST_NOTIFICATION_ID);
  }

  private void broadcastNewPostEvent(PostRealm post) {
    Intent intent = new Intent(SEND_POST_EVENT);
    intent.putExtra(KEY_ADD_POST, post);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
}
