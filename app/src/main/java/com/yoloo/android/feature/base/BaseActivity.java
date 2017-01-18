package com.yoloo.android.feature.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.feature.feed.userfeed.UserFeedController;
import com.yoloo.android.feature.login.AuthController;

public class BaseActivity extends AppCompatActivity {

  @BindView(R.id.controller_container) ViewGroup container;

  private Router router;

  private FirebaseAuth.AuthStateListener authStateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);

    ButterKnife.bind(this);

    router = Conductor.attachRouter(this, container, savedInstanceState);

    authStateListener = this::setRootController;
  }

  @Override
  protected void onStart() {
    super.onStart();
    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (authStateListener != null) {
      FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
  }

  @Override
  public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }

  private void setRootController(FirebaseAuth auth) {
    FirebaseUser user = auth.getCurrentUser();

    if (!router.hasRootController()) {
      if (user == null) {
        router.setRoot(RouterTransaction.with(new AuthController()).tag("test"));
      } else {
        router.setRoot(RouterTransaction.with(new UserFeedController()));
      }
    }
  }
}