package com.yoloo.android.feature.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.util.GoogleApiHelper;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.chat.chat.ChatController;
import com.yoloo.android.feature.explore.ExploreController;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.settings.SettingsController;
import com.yoloo.android.notificationhandler.NotificationConstants;
import com.yoloo.android.notificationhandler.NotificationResponse;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity
    implements DrawerLayoutProvider, NavigationViewProvider, ActionBarInterface,
    GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

  public static final int REQUEST_INVITE = 0;

  private static final String KEY_RELOGIN = "RELOGIN";
  private static final String KEY_SIGN_OUT = "SIGN_OUT";

  private final WeakHandler handler = new WeakHandler();

  @BindView(R.id.controller_container) ViewGroup controllerContainer;
  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

  @BindView(R.id.nav_view) NavigationView navigationView;

  private Router router;

  private Disposable disposable;

  private AccountRealm me;

  private FeedController feedController;

  @Override protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);
    ButterKnife.bind(this);

    navigationView.setNavigationItemSelectedListener(this);

    router = Conductor.attachRouter(this, controllerContainer, savedInstanceState);
    if (!router.hasRootController()) {
      SharedPreferences prefs = getSharedPreferences(KEY_RELOGIN, MODE_PRIVATE);
      if (prefs == null || prefs.getBoolean(KEY_SIGN_OUT, true)) {
        resetAuthSignedInStatus(prefs);
      } else {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
          router.setRoot(RouterTransaction.with(WelcomeController.create()));
        } else {
          UserRepository repository = UserRepositoryProvider.getRepository();
          disposable = repository.getLocalMe()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(account -> {
                me = account;
                setupDrawerInfo();
              }, Timber::e);

          feedController = FeedController.create();
          router.setRoot(RouterTransaction.with(feedController));
        }
      }
    }

    setOptionsMenuVisibility();
  }

  private void resetAuthSignedInStatus(SharedPreferences prefs) {
    AuthUI.getInstance().signOut(this);
    router.setRoot(RouterTransaction.with(WelcomeController.create()));

    if (prefs != null) {
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(KEY_SIGN_OUT, false);
      editor.apply();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    final String chatId = intent.getStringExtra(NotificationResponse.KEY_CHAT_ID);
    final String action = intent.getStringExtra(NotificationResponse.KEY_ACTION);

    if (!TextUtils.isEmpty(chatId)) {
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

  @Override public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    router.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_INVITE) {
      if (resultCode == Activity.RESULT_OK) {
        // Get the invitation IDs of all sent messages
        String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
        for (String id : ids) {
          Timber.d("onActivityResult: sent invitation: %s", id);
        }
      } else {
        // Sending failed or it was canceled, show failure message to the user
        Snackbar.make(controllerContainer, R.string.send_failed, Snackbar.LENGTH_SHORT).show();
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

  @Override public DrawerLayout getDrawerLayout() {
    return drawerLayout;
  }

  @Override public NavigationView getNavigationView() {
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

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_nav_profile:
        handler.postDelayed(
            () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
            400);
        break;
      case R.id.action_nav_explore:
        handler.postDelayed(
            () -> startTransaction(ExploreController.create(), new FadeChangeHandler()), 400);
        break;
      case R.id.action_nav_bookmarks:
        handler.postDelayed(() -> {
          PostListController controller = PostListController.ofBookmarked();
          controller.setModelUpdateEvent(feedController);
          startTransaction(controller, new FadeChangeHandler());
        }, 400);
        break;
      case R.id.action_nav_invite_friends:
        handler.postDelayed(this::onInviteClicked, 400);
        break;
      case R.id.action_nav_settings:
        handler.postDelayed(
            () -> startTransaction(SettingsController.create(), new FadeChangeHandler()), 400);
        break;
      case R.id.action_nav_feedback:
        handler.postDelayed(() -> {
          Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
          emailIntent.setData(Uri.parse("mailto: hello@yolooapp.com"));
          startActivity(Intent.createChooser(emailIntent, "Send feedback"));
        }, 400);
        break;
      default:
        break;
    }
    // Close the navigation drawer when an item is selected.
    item.setChecked(false);
    getDrawerLayout().closeDrawers();
    return true;
  }

  private void setupDrawerInfo() {
    NavigationView navigationView = getNavigationView();
    DrawerLayout drawerLayout = getDrawerLayout();

    final View headerView = navigationView.getHeaderView(0);
    final ImageView ivNavAvatar = ButterKnife.findById(headerView, R.id.iv_nav_avatar);
    final TextView tvRealname = ButterKnife.findById(headerView, R.id.tv_nav_realname);
    final TextView tvUsername = ButterKnife.findById(headerView, R.id.tv_nav_username);

    if (me != null) {
      Glide
          .with(this)
          .load(me.getAvatarUrl())
          .bitmapTransform(new CropCircleTransformation(this))
          .into(ivNavAvatar);

      tvRealname.setText(me.getRealname());
      tvUsername.setText(me.getUsername());
    }

    ivNavAvatar.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
    tvUsername.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
    tvRealname.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
  }

  private void onInviteClicked() {
    Resources res = getResources();

    Intent intent = new AppInviteInvitation.IntentBuilder(res.getString(R.string.invitation_title))
        .setMessage(res.getString(R.string.invitation_message, me.getUsername()))
        .setCustomImage(Uri.parse(res.getString(R.string.invitation_custom_image)))
        .setDeepLink(Uri.parse(res.getString(R.string.invitation_deep_link)))
        .setCallToActionText(res.getString(R.string.invitation_cta))
        .build();
    startActivityForResult(intent, REQUEST_INVITE);
  }
}
