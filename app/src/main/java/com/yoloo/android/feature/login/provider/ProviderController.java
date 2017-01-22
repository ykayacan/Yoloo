package com.yoloo.android.feature.login.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.google.android.gms.common.Scopes;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.feed.userfeed.UserFeedController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.login.FacebookProvider;
import com.yoloo.android.feature.login.GoogleProvider;
import com.yoloo.android.feature.login.IdpProvider;
import com.yoloo.android.feature.login.IdpResponse;
import com.yoloo.android.feature.login.signup.SignUpController;
import com.yoloo.android.feature.ui.widget.tagview.TagView;
import com.yoloo.android.util.LocaleUtil;
import io.reactivex.Observable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ProviderController extends MvpController<ProviderView, ProviderPresenter>
    implements ProviderView, TagView.TagsSelectListener, IdpProvider.IdpCallback {

  private static final TagView.DataTransform<CategoryRealm> TRANSFORMER = CategoryRealm::getName;

  private static final ButterKnife.Setter<View, Boolean> DEFAULT_VISIBILITY =
      (view, value, index) -> view.setVisibility(value ? View.VISIBLE : View.GONE);

  @BindView(R.id.view_login_topics) TagView tagView;

  @BindViews({
      R.id.btn_facebook_sign_in, R.id.btn_google_sign_in, R.id.tv_login_or, R.id.btn_login_sign_up
  }) View[] views;

  @BindString(R.string.error_auth_failed) String errorAuthFailedString;

  @BindString(R.string.error_google_play_services) String errorGooglePlayServicesString;

  @BindString(R.string.error_server_down) String errorServerDownString;

  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;

  @BindString(R.string.label_loading) String loadingString;

  private ProgressDialog progressDialog;

  private IdpProvider googleProvider;
  private IdpProvider facebookProvider;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_provider, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    googleProvider = getIdpProvider(AuthUI.GOOGLE_PROVIDER);
    facebookProvider = getIdpProvider(AuthUI.FACEBOOK_PROVIDER);

    googleProvider.setAuthenticationCallback(this);
    facebookProvider.setAuthenticationCallback(this);

    ButterKnife.apply(views, DEFAULT_VISIBILITY, false);

    tagView.addOnTagSelectListener(this);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ((GoogleProvider) googleProvider).connect();
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    ((GoogleProvider) googleProvider).disconnect();

    tagView.removeOnTagSelectListener(this);
    googleProvider.setAuthenticationCallback(null);
    facebookProvider.setAuthenticationCallback(null);
  }

  @NonNull @Override public ProviderPresenter createPresenter() {
    return new ProviderPresenter(UserRepository.getInstance(UserRemoteDataStore.getInstance(),
        UserDiskDataStore.getInstance()),
        CategoryRepository.getInstance(CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    googleProvider.onActivityResult(requestCode, resultCode, data);
    facebookProvider.onActivityResult(requestCode, resultCode, data);
  }

  @OnClick(R.id.btn_google_sign_in) void signUpWithGoogle() {
    googleProvider.startLogin(this);
  }

  @OnClick(R.id.btn_facebook_sign_in) void signUpWithFacebook() {
    facebookProvider.startLogin(this);
  }

  @OnClick(R.id.btn_login_sign_up) void signUp() {
    tagView.<CategoryRealm>getSelectedItemsObservable().flatMap(Observable::fromIterable)
        .map(CategoryRealm::getId)
        .toList()
        .subscribe(names -> getRouter().pushController(
            RouterTransaction.with(SignUpController.create(new ArrayList<>(names)))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler())));
  }

  @Override public void onCategoriesLoaded(List<CategoryRealm> categories) {
    tagView.setData(categories, TRANSFORMER);
  }

  @Override public void onSignedUp() {
    getParentController().getRouter()
        .setRoot(RouterTransaction.with(new UserFeedController())
            .pushChangeHandler(new FadeChangeHandler()));
  }

  @Override public void onError(Throwable t) {
    Snackbar.make(getView(), errorAuthFailedString, Snackbar.LENGTH_SHORT).show();

    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_SHORT).show();
    }

    if (t.getMessage().contains("409")) {
      Snackbar.make(getView(), errorAlreadyRegisteredString, Snackbar.LENGTH_SHORT).show();
    }

    // Backend didn't processed, sign out account.
    AuthUI.getInstance().signOut(getActivity());
  }

  @Override public void onShowLoading() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setMessage(loadingString);
      progressDialog.setIndeterminate(true);
    }

    progressDialog.show();
  }

  @Override public void onHideLoading() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override public void onItemSelected(Object item, boolean selected) {
    int size = tagView.getSelectedItems().size();

    if (selected && size >= 3) {
      ButterKnife.apply(views, DEFAULT_VISIBILITY, true);
    } else {
      if (size < 3) {
        ButterKnife.apply(views, DEFAULT_VISIBILITY, false);
      }
    }
  }

  @Override public void onSuccess(IdpResponse idpResponse) {
    final String categoryIds = tagView.<CategoryRealm>getSelectedItemsObservable().flatMap(
        categoryRealms -> Observable.fromIterable(categoryRealms).map(CategoryRealm::getId))
        .reduce((s, s2) -> s + "," + s2)
        .map(s -> s.substring(0, s.length() - 1))
        .blockingGet();

    final String locale = LocaleUtil.getCurrentLocale(getActivity()).getDisplayCountry();

    getPresenter().signUp(idpResponse, categoryIds, locale);
  }

  @Override public void onFailure(Bundle extra) {
    Snackbar.make(getView(), extra.getString("error", null), Snackbar.LENGTH_SHORT).show();
  }

  private IdpProvider getIdpProvider(String providerId) {
    final AuthUI.IdpConfig config = new AuthUI.IdpConfig.Builder(providerId).build();

    switch (providerId) {
      case AuthUI.GOOGLE_PROVIDER:
        config.addScope(Scopes.PLUS_LOGIN);
        return new GoogleProvider(this, config);
      case AuthUI.FACEBOOK_PROVIDER:
        return new FacebookProvider(getApplicationContext(), config);
      default:
        return null;
    }
  }
}