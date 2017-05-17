package com.yoloo.android.feature.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.util.GoogleApiHelper;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.chat.chat.ChatController;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.notificationhandler.NotificationConstants;
import com.yoloo.android.notificationhandler.NotificationResponse;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity
    implements DrawerLayoutProvider, NavigationViewProvider, ActionBarInterface,
    GoogleApiClient.OnConnectionFailedListener {

  public static final int REQUEST_INVITE = 0;

  private static final String KEY_RELOGIN = "RELOGIN";
  private static final String KEY_SIGN_OUT = "SIGN_OUT";

  @BindView(R.id.controller_container) ViewGroup container;
  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view) NavigationView navigationView;

  private Router router;

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
      SharedPreferences prefs = getSharedPreferences(KEY_RELOGIN, MODE_PRIVATE);
      if (prefs == null) {
        AuthUI.getInstance().signOut(this);
        router.setRoot(RouterTransaction.with(WelcomeController.create()));
      } else {
        boolean shouldSignOut = prefs.getBoolean(KEY_SIGN_OUT, true);

        if (shouldSignOut) {
          AuthUI.getInstance().signOut(this);
          router.setRoot(RouterTransaction.with(WelcomeController.create()));

          SharedPreferences.Editor editor = prefs.edit();
          editor.putBoolean(KEY_SIGN_OUT, false);
          editor.apply();
        } else {
          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

          router.setRoot(RouterTransaction.with(user == null
              ? WelcomeController.create()
              : FeedController.create()));
        }
      }
    }

    setOptionsMenuVisibility();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    final String chatId = intent.getStringExtra(NotificationResponse.KEY_CHAT_ID);
    final String action = intent.getStringExtra(NotificationResponse.KEY_ACTION);

    if (!TextUtils.isEmpty(chatId)) {
      Timber.d("Chat Id: %s", chatId);
      startTransaction(ChatController.create(chatId), new VerticalChangeHandler());
    } else if (!TextUtils.isEmpty(action)) {
      switch (action) {
        case NotificationConstants.FOLLOW:
          startTransaction(
              ProfileController.create(intent.getStringExtra(NotificationResponse.KEY_USER_ID)),
              new VerticalChangeHandler());
          break;
        case NotificationConstants.COMMENT:
          startTransaction(
              PostDetailController.create(intent.getStringExtra(NotificationResponse.KEY_POST_ID)),
              new VerticalChangeHandler());
          break;
        case NotificationConstants.MENTION:
          startTransaction(
              PostDetailController.create(intent.getStringExtra(NotificationResponse.KEY_POST_ID)),
              new VerticalChangeHandler());
          break;
        case NotificationConstants.ACCEPT:
          startTransaction(
              PostDetailController.create(intent.getStringExtra(NotificationResponse.KEY_POST_ID)),
              new VerticalChangeHandler());
          break;
        case NotificationConstants.NEW_POST:
          startTransaction(
              PostDetailController.create(intent.getStringExtra(NotificationResponse.KEY_POST_ID)),
              new VerticalChangeHandler());
          break;
      }
    }
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

    Timber.d("onActivityResult: resultCode=%s, requestCode=%s", resultCode, requestCode);

    if (requestCode == REQUEST_INVITE) {
      if (resultCode == Activity.RESULT_OK) {
        // Get the invitation IDs of all sent messages
        String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
        for (String id : ids) {
          Timber.d("onActivityResult: sent invitation: %s", id);
        }
      } else {
        // Sending failed or it was canceled, show failure message to the user
        Snackbar.make(container, R.string.send_failed, Snackbar.LENGTH_SHORT).show();
      }
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
  public DrawerLayout getDrawerLayout() {
    return drawerLayout;
  }

  @Override
  public NavigationView getNavigationView() {
    return navigationView;
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    router.pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void setupAppInvites() {
    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(AppInvite.API)
        .enableAutoManage(this, GoogleApiHelper.getSafeAutoManageId(),
            this)
        .build();

    AppInvite.AppInviteApi
        .getInvitation(googleApiClient, this, true)
        .setResultCallback(result -> {
          Timber.d("getInvitation:onResult: %s", result.getStatus());
          if (result.getStatus().isSuccess()) {
            // Extract information from the intent
            Intent intent = result.getInvitationIntent();
            String deepLink = AppInviteReferral.getDeepLink(intent);
            String invitationId = AppInviteReferral.getInvitationId(intent);

            // Because autoLaunchDeepLink = true we don't have to do anything
            // here, but we could set that to false and manually choose
            // an Activity to launch to handle the deep link here.
          }
        });
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }


}
