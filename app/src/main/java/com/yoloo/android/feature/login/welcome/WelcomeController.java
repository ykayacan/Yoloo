package com.yoloo.android.feature.login.welcome;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.login.provider.ProviderController;
import com.yoloo.android.feature.login.signin.SignInController;
import com.yoloo.android.util.HtmlUtil;

public class WelcomeController extends BaseController {

  @BindView(R.id.tv_login_action_login) TextView tvActionLogin;

  @BindString(R.string.action_login_already_have_account) String alreadyHaveAccountString;

  public static WelcomeController create() {
    return new WelcomeController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_welcome, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    tvActionLogin.setText(HtmlUtil.fromHtml(alreadyHaveAccountString));
  }

  @OnClick(R.id.btn_login_start_using) void startUsing() {
    getRouter().pushController(RouterTransaction.with(ProviderController.create())
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @OnClick(R.id.tv_login_action_login) void login() {
    getRouter().pushController(RouterTransaction.with(SignInController.create())
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }
}
