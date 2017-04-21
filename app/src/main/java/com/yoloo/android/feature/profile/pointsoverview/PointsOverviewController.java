package com.yoloo.android.feature.profile.pointsoverview;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GameInfoRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import timber.log.Timber;

public class PointsOverviewController
    extends MvpController<PointsOverviewView, PointsOverviewPresenter>
    implements PointsOverviewView {

  @BindView(R.id.toolbar_pointsoverview) Toolbar toolbar;
  @BindView(R.id.rv_history) RecyclerView rvHistory;

  private PointsHistoryAdapter adapter;

  public static PointsOverviewController create() {
    return new PointsOverviewController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_pointsoverview, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupRecyclerView();
    setupToolbar();
  }

  @NonNull @Override public PointsOverviewPresenter createPresenter() {
    return new PointsOverviewPresenter(UserRepositoryProvider.getRepository());
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(GameInfoRealm value) {
    adapter.addHistoryData(value.getHistories());
  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {
    Timber.d("Empty");
  }

  private void setupRecyclerView() {
    adapter = new PointsHistoryAdapter();

    rvHistory.setItemAnimator(new DefaultItemAnimator());
    rvHistory.addItemDecoration(
        new DividerItemDecoration(getActivity(), OrientationHelper.VERTICAL));
    rvHistory.setHasFixedSize(true);
    rvHistory.setAdapter(adapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }

    toolbar.setNavigationOnClickListener(v -> getRouter().handleBack());
  }
}
