package com.yoloo.android.feature.chat;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.data.chat.firebase.Chat;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.base.BaseActivity;
import com.yoloo.android.notificationhandler.NotificationResponse;
import com.yoloo.android.rxfirebase.FirebaseChildEvent;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class NewChatListenerService extends Service {

  public static final String NEW_MESSAGE_EVENT = "NEW_MESSAGE_EVENT";

  private static final int KEY_NEW_CHAT_ID = 200;

  private static final String KEY_CHAT_GROUP = "CHAT_GROUP";

  private final ChatRepository chatRepository = ChatRepository.getInstance();
  private final UserRepository userRepository = UserRepositoryProvider.getRepository();

  private CompositeDisposable disposable;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override public void onCreate() {
    super.onCreate();
    disposable = new CompositeDisposable();

    FirebaseAuth.getInstance().addAuthStateListener(auth -> {
      FirebaseUser user = auth.getCurrentUser();

      if (user != null) {
        Timber.d("Userrrrr");

        Disposable d = userRepository.getLocalMe()
            .map(AccountRealm::getId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(userId -> {
              Disposable d2 = getChatObservable(userId)
                  .subscribe(chat -> {
                    broadcastNewPostEvent();
                    showNotification(this, chat);
                  }, Timber::e);

              disposable.add(d2);
            }, Timber::e);

        disposable.add(d);
      } else {
        Timber.d("User is logged out! Stop Self");
        if (disposable != null && !disposable.isDisposed()) {
          disposable.dispose();
        }
        stopSelf();
      }
    });
  }

  private Observable<Chat> getChatObservable(String userId) {
    return chatRepository.observeChats(userId)
        .filter(e -> e.getEventType() == FirebaseChildEvent.EventType.CHANGED)
        .map(event -> event.getDataSnapshot().getValue(Chat.class))
        .filter(chat -> !chat.getLastSenderId().equals(userId));
  }

  private void showNotification(Context context, Chat chat) {
    Intent intent = new Intent(context, BaseActivity.class);
    intent.putExtra(NotificationResponse.KEY_ACTION, chat.getChatId());

    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    int primaryColor = ContextCompat.getColor(context, R.color.primary);

    Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle(chat.getChatName())
        .setContentText(chat.getLastMessage())
        .setSmallIcon(R.drawable.ic_yoloo_notification)
        .setGroup(KEY_CHAT_GROUP)
        .setGroupSummary(true)
        .setAutoCancel(true)
        .setColor(primaryColor)
        .setContentIntent(pendingIntent)
        .build();

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(KEY_NEW_CHAT_ID, notification);
  }

  private void broadcastNewPostEvent() {
    Intent intent = new Intent(NEW_MESSAGE_EVENT);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }
}
