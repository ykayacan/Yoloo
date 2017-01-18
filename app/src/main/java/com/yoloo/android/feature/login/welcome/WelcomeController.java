package com.yoloo.android.feature.login.welcome;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.faker.CategoryFaker;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.login.provider.ProviderController;
import com.yoloo.android.feature.login.signin.SignInController;

public class WelcomeController extends BaseController {

  static {
    CategoryFaker.generate();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_welcome, container, false);
  }

  @OnClick(R.id.btn_login_start_using)
  void startUsing() {
    getRouter().pushController(
        RouterTransaction.with(new ProviderController())
            .pushChangeHandler(new HorizontalChangeHandler())
            .popChangeHandler(new HorizontalChangeHandler()));
  }

  @OnClick(R.id.tv_login_action_login)
  void login() {
    getRouter().pushController(
        RouterTransaction.with(new SignInController())
            .pushChangeHandler(new HorizontalChangeHandler())
            .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override
  public boolean handleBack() {
    getActivity().finish();
    return true;
  }
}