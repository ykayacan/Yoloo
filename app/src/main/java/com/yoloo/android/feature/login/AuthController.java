package com.yoloo.android.feature.login;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.login.welcome.WelcomeController;
import com.yoloo.android.util.VersionUtil;

public class AuthController extends BaseController {

  @BindView(R.id.layout_login_child) ViewGroup childContainer;

  @BindView(R.id.ib_login_back) ImageButton ibBack;

  private final ControllerChangeHandler.ControllerChangeListener changeListener =
      new ControllerChangeHandler.ControllerChangeListener() {
        @Override public void onChangeStarted(@Nullable Controller to, @Nullable Controller from,
            boolean isPush, @NonNull ViewGroup container,
            @NonNull ControllerChangeHandler handler) {
          ibBack.setVisibility((to instanceof WelcomeController) ? View.GONE : View.VISIBLE);
        }

        @Override public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from,
            boolean isPush, @NonNull ViewGroup container,
            @NonNull ControllerChangeHandler handler) {
        }
      };

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_auth, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    if (VersionUtil.hasL()) {
      getActivity().getWindow().setStatusBarColor(Color.BLACK);
    }

    setChildRootController();

    ibBack.setVisibility(View.GONE);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getChildRouter(childContainer).addChangeListener(changeListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    getChildRouter(childContainer).removeChangeListener(changeListener);
    super.onDetach(view);
  }

  @OnClick(R.id.ib_login_back) void back() {
    getChildRouter(childContainer).popCurrentController();
  }

  private void setChildRootController() {
    final Router childRouter = getChildRouter(childContainer).setPopsLastView(true);

    if (!childRouter.hasRootController()) {
      childRouter.pushController(RouterTransaction.with(new WelcomeController())
          .pushChangeHandler(new FadeChangeHandler())
          .popChangeHandler(new FadeChangeHandler()));
    }
  }
}