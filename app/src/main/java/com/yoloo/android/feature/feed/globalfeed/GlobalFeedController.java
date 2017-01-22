package com.yoloo.android.feature.feed.globalfeed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import butterknife.BindColor;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.sorter.PostSorter;
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
import com.yoloo.android.feature.ui.widget.SpinnerTitleArrayAdapter;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import java.util.List;
import timber.log.Timber;

public class GlobalFeedController extends MvpController<GlobalFeedView, GlobalFeedPresenter>
    implements GlobalFeedView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnOptionsClickListener, OnReadMoreClickListener, OnShareClickListener, OnCommentClickListener,
    OnVoteClickListener, OnContentImageClickListener, OnChangeListener {

  private static final String KEY_CATEGORY_NAME = "CATEGORY_NAME";
  private static final String KEY_TAG_NAME = "TAG_NAME";

  @BindView(R.id.toolbar_feed_global) Toolbar toolbar;

  @BindView(R.id.rv_feed_global) RecyclerView rvGlobalFeed;

  @BindView(R.id.spinner_feed_global) Spinner spinner;

  @BindView(R.id.swipe_feed_global) SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.view_feed_sub_action) View actionView;

  @BindColor(R.color.primary) int colorPrimary;

  private FeedAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String categoryName;
  private String tagName;

  private String cursor;
  private String eTag;

  private boolean reEnter;

  private String itemId;

  @FeedAction private int action = FeedAction.UNSPECIFIED;

  private Object payload;

  public GlobalFeedController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static <T extends Controller & OnChangeListener> GlobalFeedController create(
      String categoryName, T targetController) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CATEGORY_NAME, categoryName).build();

    final GlobalFeedController controller = new GlobalFeedController(bundle);
    controller.setTargetController(targetController);

    return controller;
  }

  public static GlobalFeedController create(String category) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CATEGORY_NAME, category).build();

    return new GlobalFeedController(bundle);
  }

  public static GlobalFeedController createFromTag(String tag) {
    final Bundle bundle = new BundleBuilder().putString(KEY_TAG_NAME, tag).build();

    return new GlobalFeedController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed_global, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    categoryName = getArgs().getString(KEY_CATEGORY_NAME);
    tagName = getArgs().getString(KEY_TAG_NAME);

    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupSpinner();
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      if (tagName != null) {
        getPresenter().loadPostsByTag(false, cursor, eTag, 20, PostSorter.NEWEST, tagName);
      } else {
        getPresenter().loadPostsByCategory(false, cursor, eTag, 20, PostSorter.NEWEST,
            categoryName);
      }

      reEnter = true;
    }

    rvGlobalFeed.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvGlobalFeed.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    if (tagName != null) {
      return;
    }

    inflater.inflate(R.menu.menu_feed_global, menu);

    DrawableHelper.withContext(getActivity()).withColor(android.R.color.white).applyTo(menu);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        setPayload(this.itemId, action, payload);
        getRouter().popCurrentController();
        return true;
      case R.id.action_feed_sort_newest:
        reloadQuestions(item, PostSorter.NEWEST);
        return true;
      case R.id.action_feed_sort_hot:
        reloadQuestions(item, PostSorter.HOT);
        return true;
      case R.id.action_feed_sort_unanswered:
        reloadQuestions(item, PostSorter.UNANSWERED);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public boolean handleBack() {
    setPayload(itemId, action, payload);
    return super.handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();
    adapter.addPosts(value.getData());
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onEmpty() {
    actionView.setVisibility(View.VISIBLE);
  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();
    getPresenter().loadPostsByCategory(true, cursor, eTag, 20, PostSorter.NEWEST, categoryName);
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @NonNull @Override public GlobalFeedPresenter createPresenter() {
    return new GlobalFeedPresenter(PostRepository.getInstance(PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance()));
  }

  @Override public void onOptionsClick(View v, EpoxyModel<?> model, String postId, boolean self) {
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

  @Override public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    startTransaction(CommentController.create(itemId, acceptedCommentId, (long) v.getTag()),
        new VerticalChangeHandler());
  }

  @Override public void onContentImageClick(View v, String url) {

  }

  @Override public void onProfileClick(View v, String ownerId) {

  }

  @Override public void onReadMoreClickListener(View v, String postId, String acceptedCommentId,
      long modelId) {
    startTransaction(PostDetailController.create(postId, acceptedCommentId, this),
        new VerticalChangeHandler());
  }

  @Override public void onShareClick(View v) {

  }

  @Override public void onVoteClick(String votableId, int direction, @VotableType int type) {
    getPresenter().vote(votableId, direction);
  }

  @Override public void onPostUpdated(PostRealm post) {
    this.itemId = post.getId();
    this.action = FeedAction.UPDATE;
    this.payload = post;
  }

  @Override
  public void onChange(@NonNull String itemId, @FeedAction int action, @Nullable Object payload) {
    adapter.update(itemId, action, payload);
    this.itemId = itemId;
    this.action = action;
    this.payload = payload;
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

    rvGlobalFeed.setLayoutManager(layoutManager);
    rvGlobalFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvGlobalFeed.setItemAnimator(animator);

    rvGlobalFeed.setHasFixedSize(true);
    rvGlobalFeed.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setEnabled(tagName == null);

    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(colorPrimary);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(tagName != null);
      ab.setTitle(tagName);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void reloadQuestions(@NonNull MenuItem item, PostSorter sorter) {
    item.setChecked(true);
    adapter.clear();
    getPresenter().loadPostsByCategory(false, cursor, eTag, 20, sorter, categoryName);
  }

  private void setupSpinner() {
    if (tagName != null) {
      spinner.setVisibility(View.GONE);
      return;
    }

    final String[] states = getResources().getStringArray(R.array.label_sub_feed_list);

    SpinnerTitleArrayAdapter arrayAdapter = new SpinnerTitleArrayAdapter(getActivity(), states);
    arrayAdapter.setHeader(categoryName, states[0]);

    spinner.setAdapter(arrayAdapter);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

      }

      @Override public void onNothingSelected(AdapterView<?> parent) {

      }
    });
  }

  private void setPayload(String postId, @FeedAction int action, @Nullable Object payload) {
    Controller target = getTargetController();
    if (target != null) {
      ((OnChangeListener) target).onChange(postId, action, payload);
    }
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}