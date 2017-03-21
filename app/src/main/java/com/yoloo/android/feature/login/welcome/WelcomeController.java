package com.yoloo.android.feature.login.welcome;

import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.login.provider.ProviderController;
import com.yoloo.android.feature.login.signin.SignInController;
import com.yoloo.android.util.HtmlUtil;
import com.yoloo.android.util.ViewUtils;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;

public class WelcomeController extends BaseController {

  @BindView(R.id.tv_login_action_login) TextView tvActionLogin;
  @BindView(R.id.videoview_login_background) VideoView videoView;

  @BindString(R.string.action_login_already_have_account) String alreadyHaveAccountString;

  public static WelcomeController create() {
    return new WelcomeController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_welcome, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setStatusBarTransparent();

    final String fileName = "android.resource://" + getActivity().getPackageName()
        + "/raw/yoloo_login";

    videoView.setVideoURI(Uri.parse(fileName));
    videoView.start();
    videoView.setOnPreparedListener(mp -> mp.setLooping(true));

    tvActionLogin.setText(HtmlUtil.fromHtml(alreadyHaveAccountString));
  }

  @OnClick(R.id.btn_login_start_using) void startUsing() {
    startTransaction(ProviderController.create(), new HorizontalChangeHandler());
  }

  @OnClick(R.id.tv_login_action_login) void login() {
    startTransaction(SignInController.create(), new HorizontalChangeHandler());
  }

  private void startTransaction(Controller controller, ControllerChangeHandler handler) {
    getRouter().pushController(RouterTransaction.with(controller)
        .pushChangeHandler(handler)
        .popChangeHandler(handler));
  }

  private void setStatusBarTransparent() {
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
    getActivity().getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }
}
