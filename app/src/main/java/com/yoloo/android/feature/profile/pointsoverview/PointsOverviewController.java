package com.yoloo.android.feature.profile.pointsoverview;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GameInfoRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.ViewUtils;
import timber.log.Timber;

public class PointsOverviewController
    extends MvpController<PointsOverviewView, PointsOverviewPresenter>
    implements PointsOverviewView {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView rvHistory;
  @BindView(R.id.seekBar_pointsoverview) SeekBar seekBar;
  @BindView(R.id.tv_pointsoverview_initial_level_points) TextView tvInitialPoints;
  @BindView(R.id.tv_pointsoverview_next_level_points) TextView tvNextPoints;

  @BindColor(R.color.primary_dark) int colorPrimaryDark;

  private PointsHistoryAdapter adapter;

  public static PointsOverviewController create() {
    return new PointsOverviewController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_points_overview, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupRecyclerView();
    setupToolbar();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), colorPrimaryDark);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  @NonNull
  @Override
  public PointsOverviewPresenter createPresenter() {
    return new PointsOverviewPresenter(UserRepositoryProvider.getRepository());
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(GameInfoRealm value) {
    int nextLevelPoints = value.getPoints() + value.getRequiredPoints();

    tvInitialPoints.setText(String.valueOf(findInitialPointsByLevel(value.getLevel())));
    tvNextPoints.setText(String.valueOf(nextLevelPoints));
    seekBar.setMax(nextLevelPoints);
    seekBar.setProgress(value.getPoints());

    adapter.addHistoryData(value.getHistories());
  }

  @Override
  public void onError(Throwable e) {

  }

  @Override
  public void onEmpty() {
    Timber.d("Empty");
  }

  private void setupRecyclerView() {
    adapter = new PointsHistoryAdapter();

    rvHistory.setItemAnimator(new DefaultItemAnimator());
    rvHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvHistory.addItemDecoration(
        new DividerItemDecoration(getActivity(), OrientationHelper.VERTICAL));
    rvHistory.setHasFixedSize(true);
    rvHistory.setAdapter(adapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private int findInitialPointsByLevel(String level) {
    switch (level) {
      case "0":
        return 0;
      case "1":
        return 300;
      case "2":
        return 500;
      case "3":
        return 1000;
      case "4":
        return 2500;
      case "5":
        return 5000;
      default:
        return -1;
    }
  }
}
