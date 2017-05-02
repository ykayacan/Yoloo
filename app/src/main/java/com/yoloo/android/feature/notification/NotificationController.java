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
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.lottie.LottieAnimationView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import java.util.List;
import timber.log.Timber;

public class NotificationController extends MvpController<NotificationView, NotificationPresenter>
    implements NotificationView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    SwipeRefreshLayout.OnRefreshListener, OnProfileClickListener {

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.recycler_view) RecyclerView rvNotification;
  @BindView(R.id.swipe_notification) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.background_lightish) int bellColor;

  private NotificationEpoxyController epoxyController;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public NotificationController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_notification, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupPullToRefresh();
    setupRecyclerView();
    setupToolbar();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    LottieAnimationView animationView = (LottieAnimationView) ButterKnife
        .findById(rootView.getEmptyView(), R.id.empty_view)
        .findViewById(R.id.animation_view);
    Timber.d("Scale: %s", animationView.getScale());
    animationView.setScale(0.2F);
    animationView.setColorFilter(bellColor);

    Timber.d("Scale: %s", animationView.getScale());
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<NotificationRealm> notifications) {
    epoxyController.setData(notifications);
  }

  @Override
  public void showContent() {
    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onError(Throwable e) {
    rootView.setState(StateLayout.VIEW_STATE_ERROR);
    swipeRefreshLayout.setRefreshing(false);
    Timber.e(e);
  }

  @Override
  public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @NonNull
  @Override
  public NotificationPresenter createPresenter() {
    return new NotificationPresenter(NotificationRepositoryProvider.getRepository());
  }

  @Override
  public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @Override
  public void onRefresh() {
    getPresenter().loadNotifications(true, 20);
  }

  @Override
  public void onProfileClick(View v, String userId) {
    getRouter().pushController(RouterTransaction
        .with(ProfileController.create(userId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupRecyclerView() {
    epoxyController = new NotificationEpoxyController(getActivity(), this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvNotification.setLayoutManager(layoutManager);
    rvNotification.addItemDecoration(new SpaceItemDecoration(8, OrientationHelper.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvNotification.setItemAnimator(animator);

    rvNotification.setHasFixedSize(true);
    rvNotification.setAdapter(epoxyController.getAdapter());

    //endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
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
    ab.setHomeAsUpIndicator(
        AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
  }
}
