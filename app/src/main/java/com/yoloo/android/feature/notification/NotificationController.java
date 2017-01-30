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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import java.util.List;
import timber.log.Timber;

public class NotificationController extends MvpController<NotificationView, NotificationPresenter>
    implements NotificationView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    SwipeRefreshLayout.OnRefreshListener, OnProfileClickListener {

  @BindView(R.id.rv_notification) RecyclerView rvNotification;
  @BindView(R.id.swipe_notification) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar_notification) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;

  private NotificationAdapter adapter;

  private String cursor;
  private String eTag;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public NotificationController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_notification, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupPullToRefresh();
    setupRecyclerView();
    setupToolbar();
    setHasOptionsMenu(true);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    rvNotification.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvNotification.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(Response<List<NotificationRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addAll(value.getData());
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
    Timber.d(e);
  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public NotificationPresenter createPresenter() {
    return new NotificationPresenter(
        NotificationRepository.getInstance(
            NotificationRemoteDataSource.getInstance(),
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

  @Override public void onProfileClick(View v, String ownerId) {
    getRouter().pushController(RouterTransaction.with(ProfileController.create(ownerId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupRecyclerView() {
    adapter = new NotificationAdapter(this);

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

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_notification_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeAsUpIndicator(
          AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
