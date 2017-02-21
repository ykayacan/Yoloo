package com.yoloo.android.feature.base;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;
import com.yoloo.android.R;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.feature.fcm.FCMListener;
import com.yoloo.android.feature.fcm.FCMManager;
import com.yoloo.android.feature.feed.userfeed.UserFeedController;
import com.yoloo.android.feature.login.AuthController;
import com.yoloo.android.util.NotificationHelper;
import com.yoloo.android.util.Preconditions;
import com.yoloo.android.util.ViewUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;
import java.util.Map;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity implements FCMListener {

  public static final String KEY_ACTION = "action";
  public static final String KEY_DATA = "data";

  @BindView(R.id.controller_container) ViewGroup container;

  private Router router;

  private NotificationRepository notificationRepository;

  private Disposable disposable;

  private int defaultSystemVisibility;

  @Override protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);
    defaultSystemVisibility = getWindow().getDecorView().getSystemUiVisibility();

    ButterKnife.bind(this);

    router = Conductor.attachRouter(this, container, savedInstanceState);
    if (!router.hasRootController()) {
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

      if (user == null) {
        setStatusAndNavBarTransparent();
        router.setRoot(RouterTransaction.with(AuthController.create()));
      } else {
        router.setRoot(RouterTransaction.with(UserFeedController.create()));
      }
    }

    notificationRepository =
        NotificationRepository.getInstance(
            NotificationRemoteDataSource.getInstance(),
            NotificationDiskDataSource.getInstance());

    setOptionsMenuVisibility();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    FCMManager.getInstance(this).register(this);
  }

  @Override protected void onResume() {
    super.onResume();
    final Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      routeToController(bundle);
    }
  }

  @Override protected void onStop() {
    super.onStop();
    FCMManager.getInstance(this).unRegister();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    disposable.dispose();
  }

  @Override public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    router.onActivityResult(requestCode, resultCode, data);
  }

  @SuppressWarnings("unchecked") private void routeToController(Bundle bundle) {
    final String action = bundle.getString(KEY_ACTION);
    if (action != null) {
      final HashMap<String, String> data =
          (HashMap<String, String>) bundle.getSerializable(KEY_DATA);
      Preconditions.checkNotNull(data, "Data can not be null");

      switch (action) {
        case NotificationHelper.FOLLOW:
          break;
        case NotificationHelper.COMMENT:
          // TODO: 21.01.2017 Implement transaction
          //startTransaction(CommentController.ofCategory(data.getPost("qId"), ));
      }
    }
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    router.pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void setOptionsMenuVisibility() {
    router.addChangeListener(new ControllerChangeHandler.ControllerChangeListener() {
      @Override
      public void onChangeStarted(@Nullable Controller to, @Nullable Controller from,
          boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        if (from != null) {
          from.setOptionsMenuHidden(true);
        }

        if (to != null) {
          if (to instanceof UserFeedController) {
            getWindow().getDecorView().setSystemUiVisibility(defaultSystemVisibility);
          } else if (to instanceof AuthController) {
            setStatusAndNavBarTransparent();
          }
        }
      }

      @Override
      public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from,
          boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        if (from != null) {
          from.setOptionsMenuHidden(false);
        }
      }
    });
  }

  private void setStatusAndNavBarTransparent() {
    ViewUtil.setStatusBarColor(this, Color.TRANSPARENT);
    getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }

  @Override public void onDeviceRegistered(String deviceToken) {
    disposable = notificationRepository.registerFcmToken(new FcmRealm(deviceToken))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void onMessage(RemoteMessage remoteMessage) {
    sendNotification(remoteMessage.getData());
  }

  @Override public void onPlayServiceError() {

  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param data FCM message body received.
   */
  private void sendNotification(Map<String, String> data) {
    final String contentText = NotificationHelper.getRelatedNotificationString(this, data);

    Intent intent = new Intent(this, BaseActivity.class);
    intent.putExtra(BaseActivity.KEY_ACTION, data.get("action"));
    intent.putExtra(BaseActivity.KEY_DATA, new HashMap<>(data));

    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Yoloo")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

    NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0 /* ID getPost notification */, notificationBuilder.build());
  }
}
