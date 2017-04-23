package com.yoloo.android.feature.notification;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.feature.base.LceAnimator;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;
import timber.log.Timber;

public class NotificationController extends MvpController<NotificationView, NotificationPresenter>
    implements NotificationView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    SwipeRefreshLayout.OnRefreshListener, OnProfileClickListener {

  @BindView(R.id.recycler_view) RecyclerView rvNotification;
  @BindView(R.id.swipe_notification) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.loading_view) ProgressBar loadingView;
  @BindView(R.id.error_view) TextView errorView;

  @BindColor(R.color.primary) int primaryColor;

  private NotificationAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public NotificationController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_notification, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupPullToRefresh();
    setupRecyclerView();
    setupToolbar();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    rvNotification.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvNotification.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      LceAnimator.showLoading(loadingView, swipeRefreshLayout, errorView);
    }
  }

  @Override public void onLoaded(List<NotificationRealm> notifications) {
    adapter.addAll(notifications);
  }

  @Override public void showContent() {
    LceAnimator.showContent(loadingView, swipeRefreshLayout, errorView);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
    Timber.d(e);
  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public NotificationPresenter createPresenter() {
    return new NotificationPresenter(NotificationRepositoryProvider.getRepository());
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadNotifications(true, 20);
  }

  @Override public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    getRouter().pushController(RouterTransaction
        .with(ProfileController.create(userId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupRecyclerView() {
    adapter = new NotificationAdapter(getActivity(), this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvNotification.setLayoutManager(layoutManager);
    rvNotification.addItemDecoration(new SpaceItemDecoration(8, OrientationHelper.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvNotification.setItemAnimator(animator);

    rvNotification.setHasFixedSize(true);
    rvNotification.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setTitle(R.string.label_notification_title);
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setHomeAsUpIndicator(AppCompatResources.getDrawable(getActivity(),
        R.drawable.ic_close_white_24dp));
  }
}
