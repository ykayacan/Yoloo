package com.yoloo.android.feature.login.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.category.ChipAdapter;
import com.yoloo.android.feature.feed.home.FeedHomeController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.login.FacebookProvider;
import com.yoloo.android.feature.login.GoogleProvider;
import com.yoloo.android.feature.login.IdpProvider;
import com.yoloo.android.feature.login.IdpResponse;
import com.yoloo.android.feature.login.signup.SignUpController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.decoration.SpacingItemDecoration;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.LocaleUtil;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class ProviderController extends MvpController<ProviderView, ProviderPresenter>
    implements ProviderView, IdpProvider.IdpCallback,
    ChipAdapter.OnItemSelectListener<CategoryRealm> {

  private final ButterKnife.Setter<View, Boolean> visibility =
      (view, value, index) -> view.setVisibility(value ? View.VISIBLE : View.GONE);

  @BindView(R.id.rv_login_categories) RecyclerView rvLoginCategories;
  @BindViews({
      R.id.btn_facebook_sign_in, R.id.btn_google_sign_in, R.id.tv_login_or, R.id.btn_login_sign_up
  }) View[] views;

  @BindString(R.string.error_auth_failed) String errorAuthFailedString;
  @BindString(R.string.error_google_play_services) String errorGooglePlayServicesString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;
  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_unknownhost) String unknownhostString;

  @BindDrawable(R.drawable.tag_divider) Drawable tagDivider;

  private ChipAdapter<CategoryRealm> adapter;

  private ProgressDialog progressDialog;

  private IdpProvider idpProvider;

  public ProviderController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static ProviderController create() {
    return new ProviderController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_provider, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    ButterKnife.apply(views, visibility, false);
    setupRecyclerView(view.getContext());
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (idpProvider instanceof GoogleProvider) {
      ((GoogleProvider) idpProvider).connect();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (idpProvider instanceof GoogleProvider) {
      ((GoogleProvider) idpProvider).disconnect();
    }

    if (idpProvider != null) {
      idpProvider.setAuthenticationCallback(null);
    }
  }

  @NonNull @Override public ProviderPresenter createPresenter() {
    return new ProviderPresenter(UserRepository.getInstance(UserRemoteDataStore.getInstance(),
        UserDiskDataStore.getInstance()),
        CategoryRepository.getInstance(CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()),
        NotificationRepository.getInstance(NotificationRemoteDataSource.getInstance(),
            NotificationDiskDataSource.getInstance()));
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
    ArrayList<String> categoryIds = Stream.of(adapter.getSelectedItems())
        .select(ChipAdapter.ChipModel.class)
        .map(chipModel -> ((CategoryRealm) chipModel.getChipItem()))
        .map(CategoryRealm::getId)
        .collect(Collectors.toCollection(ArrayList::new));

    getRouter().pushController(RouterTransaction.with(SignUpController.create(categoryIds))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override public void onCategoriesLoaded(List<CategoryRealm> categories) {
    adapter.addChipItems(categories);
  }

  @Override public void onSignedUp() {
    getParentController().getRouter().setRoot(RouterTransaction.with(FeedHomeController.create()));
  }

  @Override public void onError(Throwable t) {
    Timber.e(t);
    Snackbar.make(getView(), errorAuthFailedString, Snackbar.LENGTH_SHORT).show();

    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_LONG).show();
    }

    if (t instanceof UnknownHostException) {
      Snackbar.make(getView(), unknownhostString, Snackbar.LENGTH_LONG).show();
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
    final String categoryIds = Stream.of(adapter.getSelectedItems())
        .select(ChipAdapter.ChipModel.class)
        .map(chipModel -> ((CategoryRealm) chipModel.getChipItem()))
        .map(CategoryRealm::getId)
        .collect(Collectors.joining(","));

    final String locale = LocaleUtil.getCurrentLocale(getActivity()).getISO3Country();

    getPresenter().signUp(idpResponse, categoryIds, locale);
  }

  @Override public void onFailure(Bundle extra) {
    //Snackbar.make(getView(), extra.getString("error", null), Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onItemSelect(View v, EpoxyModel<?> model, CategoryRealm item, boolean selected) {
    int size = adapter.getSelectedItemCount();

    final boolean showAuthButtons = size >= 3;
    ButterKnife.apply(views, visibility, showAuthButtons);
  }

  private IdpProvider getIdpProvider(String providerId) {
    final AuthUI.IdpConfig config = new AuthUI.IdpConfig.Builder(providerId).build();

    switch (providerId) {
      case AuthUI.GOOGLE_PROVIDER:
        return new GoogleProvider(this, config);
      case AuthUI.FACEBOOK_PROVIDER:
        return new FacebookProvider(config);
      default:
        throw new UnsupportedOperationException("Given providerId is not valid!");
    }
  }

  private void setupRecyclerView(Context context) {
    adapter = new ChipAdapter<>(this);

    ChipsLayoutManager lm = ChipsLayoutManager.newBuilder(context)
        .setScrollingEnabled(false)
        .setOrientation(ChipsLayoutManager.HORIZONTAL)
        .setRowStrategy(ChipsLayoutManager.STRATEGY_CENTER_DENSE)
        .withLastRow(true)
        .build();

    rvLoginCategories.setLayoutManager(lm);
    final int spacing = DisplayUtil.dpToPx(6);
    rvLoginCategories.addItemDecoration(new SpacingItemDecoration(spacing, spacing));
    rvLoginCategories.setLayoutManager(lm);
    rvLoginCategories.setHasFixedSize(true);
    rvLoginCategories.setAdapter(adapter);
  }
}
