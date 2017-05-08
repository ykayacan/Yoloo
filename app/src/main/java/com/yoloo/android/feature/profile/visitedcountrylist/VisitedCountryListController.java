package com.yoloo.android.feature.profile.visitedcountrylist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bumptech.glide.Glide;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.mikelau.countrypickerx.CountryPickerDialog;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;
import java.util.List;
import timber.log.Timber;

public class VisitedCountryListController
    extends MvpController<VisitedCountryListView, VisitedCountryListPresenter>
    implements VisitedCountryListView, CountryGridModel.OnVisitedCountryRemoveRequestListener {

  private static final String KEY_SHOWCASE_FAB = "SHOWCASE_FAB";
  private static final String KEY_USER_ID = "USER_ID";
  private static final String KEY_SELF = "SELF";

  @BindView(R.id.recycler_view) RecyclerView rvVisitedCountryList;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.fab) FloatingActionButton fab;

  private VisitedCountryListEpoxyController epoxyController;

  private CountryPickerDialog countryPicker;

  private TutoShowcase fabShowcase;

  public VisitedCountryListController(@Nullable Bundle args) {
    super(args);
  }

  public static VisitedCountryListController create(boolean self, @NonNull String userId) {
    final Bundle bundle =
        new BundleBuilder().putBoolean(KEY_SELF, self).putString(KEY_USER_ID, userId).build();
    return new VisitedCountryListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_visited_country_list, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupRecyclerview();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    boolean self = getArgs().getBoolean(KEY_SELF);
    String userId = getArgs().getString(KEY_USER_ID);

    if (self) {
      getPresenter().loadMyVisitedCountries();
      showFabTutorial();
    } else {
      getPresenter().loadVisitedCountries(userId);
      fab.setVisibility(View.GONE);
    }
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (countryPicker != null && countryPicker.isShowing()) {
      countryPicker.dismiss();
    }
  }

  @Override protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    getDrawerLayout().setFitsSystemWindows(false);
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(List<CountryRealm> value) {
    epoxyController.setData(value, null);
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
    if (e.getMessage().equals("100")) {
      Snackbar.make(getView(), R.string.visited_countries_already_added_error, Snackbar.LENGTH_LONG)
          .show();
    }
  }

  @Override public void onEmpty() {
    Timber.d("onEmpty()");
  }

  @Override public void onVisitedCountryRemoveRequest(CountryRealm country) {
    getPresenter().removeVisitedCountry(country.getCode());

    epoxyController.removeCountry(country);
  }

  @NonNull @Override public VisitedCountryListPresenter createPresenter() {
    return new VisitedCountryListPresenter(UserRepositoryProvider.getRepository());
  }

  @OnClick(R.id.fab) void openAddCountryDialog() {
    countryPicker = new CountryPickerDialog(getActivity(),
        (country, flagResId) -> getPresenter().addVisitedCountry(country.getIsoCode()), false, 0);
    countryPicker.show();
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerview() {
    epoxyController = new VisitedCountryListEpoxyController(Glide.with(getActivity()));
    epoxyController.setListener(this);

    rvVisitedCountryList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    rvVisitedCountryList.setHasFixedSize(true);
    rvVisitedCountryList.addItemDecoration(
        new GridInsetItemDecoration(3, DisplayUtil.dpToPx(4), true));
    rvVisitedCountryList.setItemAnimator(new DefaultItemAnimator());
    rvVisitedCountryList.setAdapter(epoxyController.getAdapter());
  }

  @Override public void onMeUpdated(AccountRealm me) {
    epoxyController.setData(me.getVisitedCountries(), null);
  }

  private void showFabTutorial() {
    fabShowcase = TutoShowcase.from(getActivity())
        .setContentView(R.layout.showcase_visited_countries_fab)
        .setFitsSystemWindows(true)
        .on(R.id.fab)
        .addCircle()
        .withBorder()
        .onClick(v -> fabShowcase.dismiss())
        .onClickContentView(R.id.tv_showcase_got_it, v -> fabShowcase.dismiss())
        .showOnce(KEY_SHOWCASE_FAB);
  }
}
