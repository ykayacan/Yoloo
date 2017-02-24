package com.yoloo.android.feature.login.provider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration;
import com.bluelinelabs.conductor.RouterTransaction;
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
import com.yoloo.android.feature.category.CategoryChipAdapter;
import com.yoloo.android.feature.feed.mainfeed.MainFeedController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.login.FacebookProvider;
import com.yoloo.android.feature.login.GoogleProvider;
import com.yoloo.android.feature.login.IdpProvider;
import com.yoloo.android.feature.login.IdpResponse;
import com.yoloo.android.feature.login.signup.SignUpController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.LocaleUtil;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ProviderController extends MvpController<ProviderView, ProviderPresenter>
    implements ProviderView, IdpProvider.IdpCallback, OnItemClickListener<CategoryRealm> {

  private static final ButterKnife.Setter<View, Boolean> DEFAULT_VISIBILITY =
      (view, value, index) -> view.setVisibility(value ? View.VISIBLE : View.GONE);

  @BindView(R.id.rv_login_categories) RecyclerView rvLoginCategories;
  @BindViews({
      R.id.btn_facebook_sign_in,
      R.id.btn_google_sign_in,
      R.id.tv_login_or,
      R.id.btn_login_sign_up
  }) View[] views;

  @BindString(R.string.error_auth_failed) String errorAuthFailedString;
  @BindString(R.string.error_google_play_services) String errorGooglePlayServicesString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;
  @BindString(R.string.label_loading) String loadingString;

  private CategoryChipAdapter adapter;

  private ProgressDialog progressDialog;

  private IdpProvider idpProvider;

  public ProviderController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_provider, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    ButterKnife.apply(views, DEFAULT_VISIBILITY, false);
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (idpProvider instanceof GoogleProvider) {
      ((GoogleProvider) idpProvider).connect();
    }
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (idpProvider instanceof GoogleProvider) {
      ((GoogleProvider) idpProvider).disconnect();
    }

    if (idpProvider != null) {
      idpProvider.setAuthenticationCallback(null);
    }
  }

  @NonNull @Override public ProviderPresenter createPresenter() {
    return new ProviderPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    idpProvider.onActivityResult(requestCode, resultCode, data);
  }

  @OnClick(R.id.btn_google_sign_in) void signUpWithGoogle() {
    idpProvider = getIdpProvider(AuthUI.GOOGLE_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_facebook_sign_in) void signUpWithFacebook() {
    idpProvider = getIdpProvider(AuthUI.FACEBOOK_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_login_sign_up) void signUp() {
    List<String> categoryIds = Stream.of(adapter.getSelectedCategories())
        .map(CategoryRealm::getId)
        .toList();

    getRouter().pushController(
        RouterTransaction.with(SignUpController.create(new ArrayList<>(categoryIds)))
            .pushChangeHandler(new HorizontalChangeHandler())
            .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override public void onCategoriesLoaded(List<CategoryRealm> categories) {
    adapter.addCategories(categories);
  }

  @Override public void onSignedUp() {
    getParentController().getRouter().setRoot(RouterTransaction.with(MainFeedController.create()));
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

  @Override public void onSuccess(IdpResponse idpResponse) {
    final String categoryIds = Stream.of(adapter.getSelectedCategories())
        .map(CategoryRealm::getId)
        .reduce((s1, s2) -> s1 + "," + s2)
        .get();

    final String locale = LocaleUtil.getCurrentLocale(getActivity()).getDisplayCountry();

    getPresenter().signUp(idpResponse, categoryIds, locale);
  }

  @Override public void onFailure(Bundle extra) {
    //Snackbar.make(getView(), extra.getString("error", null), Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, CategoryRealm item) {
    int size = adapter.getSelectedItemCount();

    if (size >= 3) {
      ButterKnife.apply(views, DEFAULT_VISIBILITY, true);
    } else {
      if (size < 3) {
        ButterKnife.apply(views, DEFAULT_VISIBILITY, false);
      }
    }
  }

  private IdpProvider getIdpProvider(String providerId) {
    final AuthUI.IdpConfig config = new AuthUI.IdpConfig.Builder(providerId).build();

    switch (providerId) {
      case AuthUI.GOOGLE_PROVIDER:
        config.addScope(Scopes.PLUS_LOGIN);
        return new GoogleProvider(this, config);
      case AuthUI.FACEBOOK_PROVIDER:
        return new FacebookProvider(config);
      default:
        throw new UnsupportedOperationException("Given providerId is not valid!");
    }
  }

  private void setupRecyclerView() {
    adapter = new CategoryChipAdapter(this);

    ChipsLayoutManager lm = ChipsLayoutManager.newBuilder(getActivity())
        .setMaxViewsInRow(4)
        .setRowStrategy(ChipsLayoutManager.STRATEGY_CENTER_DENSE)
        .withLastRow(true)
        .build();

    rvLoginCategories.addItemDecoration(
        new SpacingItemDecoration(DisplayUtil.dpToPx(8), DisplayUtil.dpToPx(8)));
    rvLoginCategories.setLayoutManager(lm);
    rvLoginCategories.setHasFixedSize(true);
    rvLoginCategories.setAdapter(adapter);
  }
}
