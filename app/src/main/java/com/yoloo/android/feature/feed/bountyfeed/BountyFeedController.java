package com.yoloo.android.feature.feed.bountyfeed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnChangeListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import com.yoloo.android.util.MenuHelper;
import java.util.List;
import timber.log.Timber;

public class BountyFeedController extends MvpController<BountyFeedView, BountyFeedPresenter>
    implements BountyFeedView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnOptionsClickListener, OnReadMoreClickListener, OnShareClickListener, OnCommentClickListener,
    OnVoteClickListener, OnContentImageClickListener, OnChangeListener {

  @BindView(R.id.toolbar_feed_global)
  Toolbar toolbar;

  @BindView(R.id.rv_feed_global)
  RecyclerView rvFeedSub;

  @BindView(R.id.spinner_feed_global)
  Spinner spinner;

  @BindView(R.id.swipe_feed_global)
  SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.view_feed_sub_action)
  View actionView;

  private FeedAdapter adapter;

  private String cursor;
  private String eTag;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String itemId;
  @FeedAction
  private int action = FeedAction.UNSPECIFIED;
  private Object payload;

  public static <T extends Controller & OnChangeListener> BountyFeedController create(
      T targetController) {
    final BountyFeedController controller = new BountyFeedController();
    controller.setTargetController(controller);

    return controller;
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed_global, container, false);
  }

  @Override
  protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupSpinner();
    setupRecyclerView();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    rvFeedSub.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvFeedSub.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        setPayload(this.itemId, action, payload);
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean handleBack() {
    setPayload(itemId, action, payload);
    return super.handleBack();
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override
  public void onLoaded(Response<List<PostRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();
    adapter.addPosts(value.getData());
  }

  @Override
  public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onEmpty() {
    actionView.setVisibility(View.VISIBLE);
  }

  @NonNull
  @Override
  public BountyFeedPresenter createPresenter() {
    return new BountyFeedPresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()));
  }

  @Override
  public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();
    getPresenter().loadBountyPosts(true, cursor, eTag, 20);
  }

  @Override
  public void onChange(@NonNull String itemId, @FeedAction int action, @Nullable Object payload) {
    adapter.update(itemId, action, payload);
    this.itemId = itemId;
    this.action = action;
    this.payload = payload;
  }

  @Override
  public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    getRouter().pushController(
        RouterTransaction.with(CommentController.create(itemId, acceptedCommentId,
            (long) v.getTag()))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override
  public void onContentImageClick(View v, String url) {

  }

  @Override
  public void onOptionsClick(View v, EpoxyModel<?> model, String postId, boolean self) {
    final PopupMenu optionsMenu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    if (!self) {
      optionsMenu.getMenu().getItem(1).setVisible(false);
      optionsMenu.getMenu().getItem(2).setVisible(false);
    }
    optionsMenu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_post_delete:
          getPresenter().deletePost(postId);
          adapter.delete(model);
          return true;
      }
      return false;
    });
  }

  @Override
  public void onProfileClick(View v, String ownerId) {

  }

  @Override
  public void onReadMoreClickListener(View v, String postId, String acceptedCommentId,
      long modelId) {
    getRouter().pushController(RouterTransaction.with(
        PostDetailController.create(postId, acceptedCommentId, this))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override
  public void onShareClick(View v) {

  }

  @Override
  public void onVoteClick(String votableId, int direction,
      @VotableType int type) {
    getPresenter().vote(votableId, direction);
  }

  @Override
  public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @Override
  public void onPostUpdated(PostRealm post) {
    this.itemId = post.getId();
    this.action = FeedAction.UPDATE;
    this.payload = post;
  }

  private void setupRecyclerView() {
    adapter = FeedAdapter.builder()
        .isMainFeed(false)
        .onProfileClickListener(this)
        .onReadMoreClickListener(this)
        .onOptionsClickListener(this)
        .onContentImageClickListener(this)
        .onCommentClickListener(this)
        .onShareClickListener(this)
        .onVoteClickListener(this)
        .build();

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvFeedSub.setLayoutManager(layoutManager);
    rvFeedSub.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeedSub.setItemAnimator(animator);

    rvFeedSub.setHasFixedSize(true);
    rvFeedSub.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(
        ContextCompat.getColor(getActivity(), R.color.primary));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(R.string.label_toolbar_bounty_title);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  private void setupSpinner() {
    spinner.setVisibility(View.GONE);
  }

  private void setPayload(String postId, @FeedAction int action, @Nullable Object payload) {
    Controller target = getTargetController();
    if (target != null) {
      ((OnChangeListener) target).onChange(postId, action, payload);
    }
  }
}
