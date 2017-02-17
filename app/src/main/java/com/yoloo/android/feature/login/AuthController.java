package com.yoloo.android.feature.login;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.login.welcome.WelcomeController;
import com.yoloo.android.util.ViewUtil;

public class AuthController extends BaseController {

  @BindView(R.id.layout_login_child) ViewGroup childContainer;
  @BindView(R.id.toolbar_auth) Toolbar toolbar;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  public static AuthController create() {
    return new AuthController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_auth, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupToolbar();
    setHasOptionsMenu(true);
    setChildRootController();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtil.setStatusBarColor(getActivity(), Color.BLACK);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    ViewUtil.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setChildRootController() {
    final Router childRouter = getChildRouter(childContainer).setPopsLastView(true);

    if (!childRouter.hasRootController()) {
      childRouter.setRoot(RouterTransaction.with(WelcomeController.create()));

      childRouter.addChangeListener(new ControllerChangeHandler.ControllerChangeListener() {
        @Override
        public void onChangeStarted(@Nullable Controller to, @Nullable Controller from,
            boolean isPush, @NonNull ViewGroup container,
            @NonNull ControllerChangeHandler handler) {
        }

        @Override
        public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from,
            boolean isPush, @NonNull ViewGroup container,
            @NonNull ControllerChangeHandler handler) {
          final boolean showBackArrow = !(to instanceof WelcomeController);
          getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
          getSupportActionBar().setDisplayShowHomeEnabled(showBackArrow);
        }
      });
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(false);
      ab.setDisplayShowHomeEnabled(false);
    }
  }
}