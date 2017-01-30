package com.yoloo.android.feature.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.yoloo.android.R;
import com.yoloo.android.feature.feed.userfeed.UserFeedController;
import com.yoloo.android.feature.login.AuthController;
import com.yoloo.android.util.NotificationHelper;
import com.yoloo.android.util.Preconditions;
import java.util.HashMap;

public class BaseActivity extends AppCompatActivity {

  public static final String KEY_ACTION = "action";
  public static final String KEY_DATA = "data";

  @BindView(R.id.controller_container) ViewGroup container;

  private Router router;

  private FirebaseAuth.AuthStateListener authStateListener;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);

    ButterKnife.bind(this);

    router = Conductor.attachRouter(this, container, savedInstanceState);
    authStateListener = BaseActivity.this::setRootController;

    setOptionsMenuVisibility();

  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
  }

  @Override protected void onResume() {
    super.onResume();
    final Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      routeToController(bundle);
    }
  }

  @Override public void onStop() {
    super.onStop();
    if (authStateListener != null) {
      FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
  }

  @Override public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    router.onActivityResult(requestCode, resultCode, data);
  }

  private void setRootController(FirebaseAuth auth) {
    FirebaseUser user = auth.getCurrentUser();

    if (!router.hasRootController()) {
      if (user == null) {
        router.setRoot(RouterTransaction.with(new AuthController()));
      } else {
        router.setRoot(RouterTransaction.with(new UserFeedController()));
      }
    }
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
          //startTransaction(CommentController.ofCategory(data.get("qId"), ));
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
        if (from != null) from.setOptionsMenuHidden(true);
      }

      @Override
      public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from,
          boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
        if (from != null) from.setOptionsMenuHidden(false);
      }
    });
  }
}