package com.yoloo.android.feature.editor.job;

import android.app.Notification;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.google.firebase.FirebaseException;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.util.NotificationUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.UUID;
import org.parceler.Parcels;
import timber.log.Timber;

public final class SendPostJob extends Job {

  public static final String TAG = "job_send_post";

  public static final int NOTIFICATION_SENDING_ID = 1;
  public static final int NOTIFICATION_ERROR_ID = 2;

  public static final String SEND_POST_EVENT = "SEND_POST_EVENT";
  public static final String KEY_ADD_POST = "ADD_POST";

  private final PostRepository postRepository;

  public SendPostJob(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  public static void scheduleJob() {
    new JobRequest.Builder(SendPostJob.TAG)
        .setExecutionWindow(3_000L, 4_000L)
        .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
        .setBackoffCriteria(5000L, JobRequest.BackoffPolicy.EXPONENTIAL)
        .setPersisted(true)
        .build()
        .schedule();
  }

  @NonNull
  @Override
  protected Result onRunJob(Params params) {
    Throwable throwable = postRepository
        .getDraft()
        .doOnSubscribe(disposable -> showSendingNotification())
        .map(this::prepareDraft)
        .flatMap(postRepository::addPost)
        .doOnSuccess(this::broadcastNewPostEvent)
        .flatMapCompletable(ignored -> postRepository.deleteDraft())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(ignored -> NotificationUtil.cancel(NOTIFICATION_SENDING_ID))
        .doOnError(this::showErrorNotification)
        .doOnComplete(() -> NotificationUtil.cancel(NOTIFICATION_SENDING_ID))
        .blockingGet();

    Timber.e(throwable);

    return Result.SUCCESS;
  }

  private PostRealm prepareDraft(PostRealm draft) {
    return draft.setId(UUID.randomUUID().toString()).setFeedItem(true).setPending(false);
  }

  private void broadcastNewPostEvent(PostRealm post) {
    Intent intent = new Intent(SEND_POST_EVENT);
    intent.putExtra(KEY_ADD_POST, Parcels.wrap(post));
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
  }

  private void showSuccessfulNotification() {
    Notification notification = new Notification.Builder(getContext())
        .setTicker(getContext().getString(R.string.label_sending_post_successful))
        .setContentTitle(getContext().getString(R.string.label_sending_post_successful))
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_cloud_done_white_24dp)
        .setLocalOnly(true)
        .setOngoing(false)
        .build();
  }

  private void showSendingNotification() {
    Notification notification = new Notification.Builder(getContext())
        .setTicker(getContext().getString(R.string.label_sending_post))
        .setContentTitle(getContext().getString(R.string.label_sending_post))
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setLocalOnly(true)
        .setSmallIcon(R.drawable.ic_cloud_upload_white_24dp)
        .setProgress(0, 100, true)
        .build();

    NotificationUtil.show(notification, NOTIFICATION_SENDING_ID);
  }

  private void showErrorNotification(Throwable throwable) {
    final String error = getContext().getString(throwable instanceof FirebaseException
        ? R.string.error_network_unavailable
        : R.string.label_sending_post_failed);

    Notification notification = new Notification.Builder(getContext())
        .setTicker(error)
        .setContentTitle(error)
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_error_black_24dp)
        .setOngoing(false)
        .setLocalOnly(true)
        .build();

    NotificationUtil.show(notification, NOTIFICATION_ERROR_ID);
  }
}
