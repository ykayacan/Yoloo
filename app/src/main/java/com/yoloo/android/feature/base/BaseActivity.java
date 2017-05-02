package com.yoloo.android.feature.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.fastaccess.datetimepicker.callback.DatePickerCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;
import com.yoloo.android.R;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.fcm.FCMListener;
import com.yoloo.android.fcm.FCMManager;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.chat.NewChatListenerService;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.notificationhandler.NotificationConstants;
import com.yoloo.android.notificationhandler.NotificationHandler;
import com.yoloo.android.util.Preconditions;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity
    implements FCMListener, DrawerLayoutProvider, NavigationViewProvider, ActionBarInterface,
    DatePickerCallback {

  public static final String KEY_ACTION = "action";
  public static final String KEY_DATA = "data";

  @BindView(R.id.controller_container) ViewGroup container;
  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view) NavigationView navigationView;

  private Router router;
  private OnDatePickListener onDatePickListener;

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);
    ButterKnife.bind(this);

    router = Conductor.attachRouter(this, container, savedInstanceState);
    if (!router.hasRootController()) {
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

      Controller controller = user == null ? WelcomeController.create() : FeedController.create();
      router.setRoot(RouterTransaction.with(controller));
    }

    setOptionsMenuVisibility();

    startService(new Intent(this, NewChatListenerService.class));
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();
    FCMManager.getInstance(this).register(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    final Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      routeToController(bundle);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    FCMManager.getInstance(this).unRegister();
  }

  @Override
  public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    router.onActivityResult(requestCode, resultCode, data);
  }

  @SuppressWarnings("unchecked")
  private void routeToController(Bundle bundle) {
    final String action = bundle.getString(KEY_ACTION);
    if (action != null) {
      final HashMap<String, String> data =
          (HashMap<String, String>) bundle.getSerializable(KEY_DATA);
      Preconditions.checkNotNull(data, "Data can not be null");
    }
  }

  private void setOptionsMenuVisibility() {
    router.addChangeListener(new ControllerChangeHandler.ControllerChangeListener() {
      @Override
      public void onChangeStarted(@Nullable Controller to, @Nullable Controller from,
          boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        if (from != null) {
          from.setOptionsMenuHidden(true);
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

  @Override
  public void onDeviceRegistered(String deviceToken) {
    NotificationRepositoryProvider
        .getRepository()
        .registerFcmToken(new FcmRealm(deviceToken))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);
  }

  @Override
  public void onMessage(RemoteMessage remoteMessage) {
    Timber.d("onMessage(): %s", remoteMessage.getData().toString());
    Map<String, String> data = remoteMessage.getData();

    Intent intent = new Intent(this, BaseActivity.class);
    intent.putExtra(BaseActivity.KEY_ACTION, data.get(NotificationConstants.KEY_ACTION));
    intent.putExtra(BaseActivity.KEY_DATA, new HashMap<>(data));

    try {
      NotificationHandler.getInstance().handle(remoteMessage,this, intent);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPlayServiceError() {

  }

  @Override
  public DrawerLayout getDrawerLayout() {
    return drawerLayout;
  }

  @Override
  public NavigationView getNavigationView() {
    return navigationView;
  }

  @Override
  public void onDateSet(long date) {
    onDatePickListener.onDatePick(date);
  }

  public void setOnDatePickListener(OnDatePickListener onDatePickListener) {
    this.onDatePickListener = onDatePickListener;
  }

  public interface OnDatePickListener {
    void onDatePick(long date);
  }
}
