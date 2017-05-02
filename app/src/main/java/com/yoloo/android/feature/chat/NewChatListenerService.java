package com.yoloo.android.feature.chat;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.data.model.chat.Dialog;
import com.yoloo.android.data.model.chat.User;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import java.util.Map;

public class NewChatListenerService extends Service {

  private static final int KEY_NEW_CHAT_ID = 200;

  private ChatRepository chatRepository = ChatRepository.getInstance();
  private UserRepository userRepository = UserRepositoryProvider.getRepository();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    /*userRepository
        .getLocalMe()
        .map(AccountRealm::getId)
        .flatMapObservable(userId -> Observable.zip(Observable.just(userId),
            chatRepository.getMessagesByDialogId(userId), Pair::create))
        .skip(1)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          Dialog dialog = pair.second.getDataSnapshot().getValue(Dialog.class);
          Timber.d("Dialog: %s", dialog);

          switch (pair.second.getEventType()) {
            case ADDED:
              if (dialog != null) {
                //showNotification(pair.first, dialog);
              }
              break;
            case CHANGED:
              break;
            case REMOVED:
              break;
            case MOVED:
              break;
          }
        });*/
  }

  private void showNotification(String userId, Dialog dialog) {
    // Build the notification, setting the group appropriately
    Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle("New message from " + getTargetUsername(userId, dialog))
        .setContentText(dialog.getLastMessageString())
        .setSmallIcon(R.drawable.ic_email_black_24dp)
        .setGroup(dialog.getId())
        .build();

    // Issue the notification
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(KEY_NEW_CHAT_ID, notification);
  }

  private Stream<String> getTargetUsername(String userId, Dialog dialog) {
    return Stream
        .of(dialog.getMembers())
        .filterNot(value -> value.getKey().equals(userId))
        .map(Map.Entry::getValue)
        .map(User::getName);
  }
}
