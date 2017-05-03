package com.yoloo.android.feature.profile.visitedcountrylist;

import android.support.annotation.NonNull;
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
import com.mikelau.countrypickerx.CountryPickerDialog;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.DisplayUtil;
import java.util.List;
import timber.log.Timber;

public class VisitedCountryListController
    extends MvpController<VisitedCountryListView, VisitedCountryListPresenter>
    implements VisitedCountryListView {

  @BindView(R.id.recycler_view) RecyclerView rvVisitedCountryList;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private VisitedCountryListEpoxyController epoxyController;

  private CountryPickerDialog countryPicker;

  public static VisitedCountryListController create() {
    return new VisitedCountryListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_visited_country_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupRecyclerview();
    getDrawerLayout().setFitsSystemWindows(false);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (countryPicker != null && countryPicker.isShowing()) {
      countryPicker.dismiss();
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<CountryRealm> value) {
    epoxyController.setData(value);
  }

  @Override
  public void onError(Throwable e) {
    Timber.e(e);
    if (e.getMessage().equals("100")) {
      Snackbar.make(getView(), "Country is already added!", Snackbar.LENGTH_LONG).show();
    }
  }

  @Override
  public void onEmpty() {
    Timber.d("onEmpty()");
  }

  @NonNull
  @Override
  public VisitedCountryListPresenter createPresenter() {
    return new VisitedCountryListPresenter(UserRepositoryProvider.getRepository());
  }

  @OnClick(R.id.fab)
  void openAddCountryDialog() {
    countryPicker = new CountryPickerDialog(getActivity(),
        (country, flagResId) -> getPresenter().addVisitedCountry(country.getIsoCode()), false, 0);
    countryPicker.show();
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Visited Countries");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerview() {
    epoxyController = new VisitedCountryListEpoxyController(Glide.with(getActivity()));

    rvVisitedCountryList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    rvVisitedCountryList.setHasFixedSize(true);
    rvVisitedCountryList.addItemDecoration(
        new GridInsetItemDecoration(3, DisplayUtil.dpToPx(4), true));
    rvVisitedCountryList.setItemAnimator(new DefaultItemAnimator());
    rvVisitedCountryList.setAdapter(epoxyController.getAdapter());
  }

  @Override
  public void onMeUpdated(AccountRealm me) {
    epoxyController.setData(me.getVisitedCountries());
  }
}
