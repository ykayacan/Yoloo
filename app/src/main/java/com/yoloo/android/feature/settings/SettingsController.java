package com.yoloo.android.feature.settings;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.yoloo.android.BuildConfig;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.base.BaseController;
import io.realm.Realm;

public class SettingsController extends BaseController {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.tv_version) TextView tvVersion;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;

  public static SettingsController create() {
    return new SettingsController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_settings, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Settings");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    tvVersion.setText(BuildConfig.VERSION_NAME);
  }

  @OnClick(R.id.iv_facebook_icon)
  void openFacebook() {
    String url = "https://www.facebook.com/yolooapp";
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.setToolbarColor(primaryColor);
    builder.addDefaultShareMenuItem();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
  }

  @OnClick(R.id.iv_instagram_icon)
  void openInstagram() {
    String url = "https://www.instagram.com/yolooapp";
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.setToolbarColor(primaryColor);
    builder.addDefaultShareMenuItem();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
  }

  @OnClick(R.id.iv_twitter_icon)
  void openTwitter() {
    String url = "https://twitter.com/yolooapp";
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.setToolbarColor(primaryColor);
    builder.addDefaultShareMenuItem();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
  }

  @OnClick(R.id.tv_privacy_policy)
  void openPrivacyPolicy() {
    String url = "http://yolooapp.com/privacy.html";
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
  }

  @OnClick(R.id.tv_log_out)
  void signOut() {
    AuthUI.getInstance().signOut((FragmentActivity) getActivity());
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> Realm.getDefaultInstance().deleteAll());
    realm.close();

    getRouter().setRoot(RouterTransaction
        .with(WelcomeController.create())
        .pushChangeHandler(new FadeChangeHandler()));
  }
}
