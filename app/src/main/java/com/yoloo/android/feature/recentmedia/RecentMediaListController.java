package com.yoloo.android.feature.recentmedia;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.feature.blog.BlogController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.DisplayUtil;
import java.util.List;
import timber.log.Timber;

public class RecentMediaListController
    extends MvpController<RecentMediaListView, RecentMediaListPresenter>
    implements RecentMediaListView, OnItemClickListener<PostRealm>,
    SwipeRefreshLayout.OnRefreshListener {

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;

  @BindColor(R.color.primary) int primaryColor;

  private RecentMediaListEpoxyController epoxyController;

  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  private boolean reEnter;

  public static RecentMediaListController create() {
    return new RecentMediaListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_recentmedia_list, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupPullToRefresh();
    setupRecyclerView();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      getPresenter().loadRecentMedias(false);
      reEnter = true;
    }

    rootView.setViewStateListener((stateView, viewState) -> {
      if (viewState == StateLayout.VIEW_STATE_ERROR) {
        View errorActionView = ButterKnife.findById(stateView, R.id.error_view);
        errorActionView.setOnClickListener(v -> getPresenter().loadRecentMedias(true));
      }
    });
  }

  @Override public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override public void onLoaded(List<FeedItem<?>> value) {
    epoxyController.setData(value, false);
    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onMoreDataLoaded(List<FeedItem<?>> items) {
    if (items.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(items);
    }
  }

  @Override public void onError(Throwable e) {
    rootView.setState(StateLayout.VIEW_STATE_ERROR);
    swipeRefreshLayout.setRefreshing(false);
    Timber.e(e);
  }

  @Override public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onRefresh() {
    endlessRecyclerOnScrollListener.resetState();
    getPresenter().loadRecentMedias(true);
  }

  @NonNull @Override public RecentMediaListPresenter createPresenter() {
    return new RecentMediaListPresenter(PostRepositoryProvider.getRepository());
  }

  private void setupRecyclerView() {
    epoxyController = new RecentMediaListEpoxyController(getActivity());
    epoxyController.setOnItemClickListener(this);

    GridLayoutManager glm = new GridLayoutManager(getActivity(), 3);
    recyclerView.setLayoutManager(glm);
    recyclerView.addItemDecoration(
        new GridInsetItemDecoration(3, DisplayUtil.dpToPx(2), false));
    recyclerView.setItemAnimator(new SlideInItemAnimator());
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(epoxyController.getAdapter());

    endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(glm) {
      @Override public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
        getPresenter().loadMoreRecentMedias();
        epoxyController.showLoader();
      }
    };
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.recent_medias_title);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  @Override public void onItemClick(View v, PostRealm item) {
    Controller controller;
    if (item.isBlogPost()) {
      controller = BlogController.create(item);
    } else {
      controller = PostDetailController.create(item.getId());
    }

    getRouter().pushController(RouterTransaction.with(controller)
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }
}
