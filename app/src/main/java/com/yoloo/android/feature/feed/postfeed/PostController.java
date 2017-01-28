package com.yoloo.android.feature.feed.postfeed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.event.DeleteEvent;
import com.yoloo.android.feature.feed.common.event.UpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import com.yoloo.android.feature.ui.widget.SpinnerTitleArrayAdapter;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.RxBus;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.disposables.Disposable;
import java.util.List;
import timber.log.Timber;

public class PostController extends MvpController<PostView, PostPresenter>
    implements PostView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnOptionsClickListener, OnReadMoreClickListener, OnShareClickListener, OnCommentClickListener,
    OnVoteClickListener, OnContentImageClickListener {

  private static final int VIEW_TYPE_CATEGORY = 0;
  private static final int VIEW_TYPE_TAGS = 1;
  private static final int VIEW_TYPE_BOUNTY = 2;
  private static final int VIEW_TYPE_BOOKMARKED = 3;
  private static final int VIEW_TYPE_USER = 4;

  private static final String KEY_CATEGORY_NAME = "CATEGORY_NAME";
  private static final String KEY_TAG_NAME = "TAG_NAME";
  private static final String KEY_USER_ID = "USER_ID";
  private static final String KEY_COMMENTED = "COMMENTED";
  private static final String KEY_BOUNTY = "BOUNTY";
  private static final String KEY_BOOKMARKED = "BOOKMARKED";

  @BindView(R.id.toolbar_feed_global) Toolbar toolbar;
  @BindView(R.id.rv_feed_global) RecyclerView rvGlobalFeed;
  @BindView(R.id.spinner_feed_global) Spinner spinner;
  @BindView(R.id.swipe_feed_global) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.view_feed_sub_action) View actionView;

  @BindColor(R.color.primary) int primaryColor;

  private FeedAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private WeakHandler handler = new WeakHandler();

  private int viewType;

  private String categoryName;
  private String tagName;
  private String userId;
  private boolean isCommented;
  private boolean isBounty;
  private boolean isBookmarked;

  private String cursor;
  private String eTag;

  private boolean reEnter;

  private Disposable disposable;

  private String currentUserId;

  public PostController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static PostController ofBounty() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOUNTY, true).build();

    return new PostController(bundle);
  }

  public static PostController ofBookmarked() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOOKMARKED, true).build();

    return new PostController(bundle);
  }

  public static PostController ofCategory(String categoryName) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CATEGORY_NAME, categoryName).build();

    return new PostController(bundle);
  }

  public static PostController ofTag(String tagName) {
    final Bundle bundle = new BundleBuilder().putString(KEY_TAG_NAME, tagName).build();

    return new PostController(bundle);
  }

  public static PostController ofUser(String userId, boolean commented) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putBoolean(KEY_COMMENTED, commented)
        .build();

    return new PostController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed_global, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    final Bundle args = getArgs();

    categoryName = args.getString(KEY_CATEGORY_NAME);
    tagName = args.getString(KEY_TAG_NAME);
    userId = args.getString(KEY_USER_ID);
    isCommented = args.getBoolean(KEY_COMMENTED, false);
    isBounty = args.getBoolean(KEY_BOUNTY, false);
    isBookmarked = args.getBoolean(KEY_BOOKMARKED, false);

    setViewType();

    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupSpinner();
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      chooseLoadMethod();
      reEnter = true;
    }

    disposable = RxBus.get().observeEvents(getClass())
        .doOnNext(
            event -> RxBus.get().sendEvent(event, ControllerUtil.getPreviousControllerClass(this)))
        .subscribe(e -> {
          if (e instanceof UpdateEvent) {
            adapter.updatePost(FeedAction.UPDATE, ((UpdateEvent) e).getPost());
          } else if (e instanceof DeleteEvent) {
            adapter.updatePost(FeedAction.DELETE, ((DeleteEvent) e).getPost());
          }
        });

    rvGlobalFeed.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvGlobalFeed.removeOnScrollListener(endlessRecyclerViewScrollListener);

    disposable.dispose();
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    // Show toolbar only if category name exists.
    if (!TextUtils.isEmpty(categoryName)) {
      inflater.inflate(R.menu.menu_feed_global, menu);

      DrawableHelper.withContext(getActivity()).withColor(android.R.color.white).applyTo(menu);
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
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

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.showLoadMoreIndicator(false);
    endlessRecyclerViewScrollListener.setProgressBarVisible(false);
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

    chooseLoadMethod();
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
    endlessRecyclerViewScrollListener.setProgressBarVisible(true);
    handler.postDelayed(this::chooseLoadMethod, 700);
  }

  @Override public void onAccountLoaded(AccountRealm account) {
    currentUserId = account.getId();
  }

  @NonNull @Override public PostPresenter createPresenter() {
    return new PostPresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()
        ));
  }

  @Override
  public void onOptionsClick(View v, EpoxyModel<?> model, String postId, String postOwnerId) {
    final PopupMenu optionsMenu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    final boolean self = currentUserId.equals(postOwnerId);
    optionsMenu.getMenu().getItem(1).setVisible(self);
    optionsMenu.getMenu().getItem(2).setVisible(self);

    optionsMenu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_feed_popup_bookmark:
          getPresenter().bookmarkPost(postId);
          Snackbar.make(getView(), R.string.label_feed_bookmarked, Snackbar.LENGTH_SHORT).show();
          return true;
        case R.id.action_feed_popup_edit:
          return true;
        case R.id.action_feed_popup_delete:
          getPresenter().deletePost(postId);
          adapter.delete(model);
          return true;
      }
      return false;
    });
  }

  @Override public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    startTransaction(CommentController.create(itemId), new VerticalChangeHandler());
  }

  @Override public void onContentImageClick(View v, String url) {
    startTransaction(PhotoController.create(url), new FadeChangeHandler());
  }

  @Override public void onProfileClick(View v, String ownerId) {
    startTransaction(ProfileController.create(ownerId), new VerticalChangeHandler());
  }

  @Override public void onReadMoreClickListener(View v, String postId) {
    startTransaction(PostDetailController.create(postId), new VerticalChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override public void onPostUpdated(PostRealm post) {
    RxBus.get().sendEvent(new UpdateEvent(post), ControllerUtil.getPreviousControllerClass(this));
  }

  private void setupRecyclerView() {
    adapter = FeedAdapter.builder()
        .isMainFeed(false)
        .context(getApplicationContext())
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
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setDisplayShowHomeEnabled(true);

    if (viewType == VIEW_TYPE_USER) {
      toolbar.setVisibility(View.GONE);
    } else if (viewType == VIEW_TYPE_CATEGORY) {
      ab.setDisplayShowTitleEnabled(false);
    } else if (viewType == VIEW_TYPE_TAGS) {
      ab.setTitle(tagName);
    } else if (viewType == VIEW_TYPE_BOUNTY) {
      ab.setTitle(R.string.label_toolbar_bounty_title);
    } else if (viewType == VIEW_TYPE_BOOKMARKED) {
      ab.setTitle(R.string.label_toolbar_bookmark_title);
    }
  }

  private void reloadQuestions(@NonNull MenuItem item, PostSorter sorter) {
    item.setChecked(true);
    adapter.clear();
    getPresenter().loadPostsByCategory(false, categoryName, sorter, cursor, eTag, 20);
  }

  private void setupSpinner() {
    if (viewType != VIEW_TYPE_CATEGORY) {
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

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void chooseLoadMethod() {
    if (viewType == VIEW_TYPE_USER) {
      getPresenter().loadPostsByUser(false, userId, isCommented, cursor, eTag, 20);
    } else if (viewType == VIEW_TYPE_TAGS) {
      getPresenter().loadPostsByTag(false, tagName, PostSorter.NEWEST, cursor, eTag, 20);
    } else if (viewType == VIEW_TYPE_CATEGORY) {
      getPresenter().loadPostsByCategory(false, categoryName, PostSorter.NEWEST, cursor, eTag, 20);
    } else if (viewType == VIEW_TYPE_BOUNTY) {
      getPresenter().loadPostsByBounty(false, cursor, eTag, 20);
    } else if (viewType == VIEW_TYPE_BOOKMARKED) {
      getPresenter().loadPostsByBookmarked(false, cursor, eTag, 20);
    }
  }

  private void setViewType() {
    final Bundle args = getArgs();
    categoryName = args.getString(KEY_CATEGORY_NAME);
    tagName = args.getString(KEY_TAG_NAME);
    userId = args.getString(KEY_USER_ID);
    isCommented = args.getBoolean(KEY_COMMENTED, false);
    isBounty = args.getBoolean(KEY_BOUNTY, false);
    isBookmarked = args.getBoolean(KEY_BOOKMARKED, false);

    if (!TextUtils.isEmpty(categoryName)) {
      viewType = VIEW_TYPE_CATEGORY;
    } else if (!TextUtils.isEmpty(tagName)) {
      viewType = VIEW_TYPE_TAGS;
    } else if (!TextUtils.isEmpty(userId)) {
      viewType = VIEW_TYPE_USER;
    } else if (isBounty) {
      viewType = VIEW_TYPE_BOUNTY;
    } else if (isBookmarked) {
      viewType = VIEW_TYPE_BOOKMARKED;
    }
  }
}