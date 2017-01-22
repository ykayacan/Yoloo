package com.yoloo.android.feature.notification;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import java.util.List;
import timber.log.Timber;

public class NotificationController extends MvpController<NotificationView, NotificationPresenter>
    implements NotificationView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    SwipeRefreshLayout.OnRefreshListener {

  @BindView(R.id.rv_notification) RecyclerView rvNotification;

  @BindView(R.id.swipe_notification) SwipeRefreshLayout swipeRefreshLayout;

  @BindColor(R.color.primary) int primaryColor;

  private NotificationAdapter adapter;

  private String cursor;
  private String eTag;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_notification, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupPullToRefresh();
    setupRecyclerView();
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
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<NotificationRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addAll(value.getData());
  }

  @Override public void onError(Throwable e) {
    Timber.d(e);
  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public NotificationPresenter createPresenter() {
    return new NotificationPresenter(
        NotificationRepository.getInstance(NotificationRemoteDataSource.getInstance(),
            NotificationDiskDataSource.getInstance()));
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadNotifications(true, cursor, 20);
  }

  @OnClick(R.id.close) void close() {
    getRouter().popCurrentController();
  }

  private void setupRecyclerView() {
    adapter = new NotificationAdapter();

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
}
