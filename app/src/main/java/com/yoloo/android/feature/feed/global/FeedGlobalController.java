package com.yoloo.android.feature.feed.global;

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
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
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
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.EditorType;
import com.yoloo.android.feature.editor.editorcategorylist.EditorCategoryListController;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.SpinnerTitleArrayAdapter;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.WeakHandler;
import java.util.List;
import timber.log.Timber;

public class FeedGlobalController extends MvpController<FeedGlobalView, FeedGlobalPresenter>
    implements FeedGlobalView, SwipeRefreshLayout.OnRefreshListener,
    OnProfileClickListener, OnPostOptionsClickListener, OnReadMoreClickListener,
    OnShareClickListener, OnCommentClickListener, OnVoteClickListener,
    OnContentImageClickListener, OnModelUpdateEvent {

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

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.toolbar_feed_global) Toolbar toolbar;
  @BindView(R.id.rv_feed_global) RecyclerView rvPostFeed;
  @BindView(R.id.spinner_feed_global) Spinner spinner;
  @BindView(R.id.swipe_feed_global) SwipeRefreshLayout swipeRefreshLayout;

  @BindColor(R.color.primary) int primaryColor;

  private FeedGlobalAdapter adapter;

  private WeakHandler handler;

  private int viewType;

  private String categoryName;
  private String tagName;
  private String userId;
  private boolean isCommented;
  private boolean isBounty;
  private boolean isBookmarked;

  private boolean reEnter;

  private String currentUserId;

  private OnModelUpdateEvent modelUpdateEvent;

  public FeedGlobalController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static FeedGlobalController ofBounty() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOUNTY, true).build();

    return new FeedGlobalController(bundle);
  }

  public static FeedGlobalController ofBookmarked() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOOKMARKED, true).build();

    return new FeedGlobalController(bundle);
  }

  public static FeedGlobalController ofCategory(String categoryName) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CATEGORY_NAME, categoryName).build();

    return new FeedGlobalController(bundle);
  }

  public static FeedGlobalController ofTag(String tagName) {
    final Bundle bundle = new BundleBuilder().putString(KEY_TAG_NAME, tagName).build();

    return new FeedGlobalController(bundle);
  }

  public static FeedGlobalController ofUser(String userId, boolean commented) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putBoolean(KEY_COMMENTED, commented)
        .build();

    return new FeedGlobalController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed_global, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    handler = new WeakHandler();

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

    rootView.setViewStateListener((stateView, viewState) -> {
      if (viewState == StateLayout.VIEW_STATE_EMPTY) {
        ButterKnife.findById(stateView, R.id.btn_empty_action)
            .setOnClickListener(v -> {
              startTransaction(EditorCategoryListController.create(EditorType.ASK_QUESTION),
                  new VerticalChangeHandler());
            });
      }
    });
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      chooseLoadMethod(false);
      reEnter = true;
    }
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    if (viewType == VIEW_TYPE_CATEGORY) {
      inflater.inflate(R.menu.menu_feed_global, menu);

      DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      case R.id.action_feed_search:
        startTransaction(SearchController.create(), new VerticalChangeHandler());
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
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    adapter.showFooter(rvPostFeed, false);
    adapter.addPosts(value.getData());
  }

  @Override public void showContent() {
    Timber.d("showContent()");
    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
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
    adapter.clear();
    adapter.showFooter(rvPostFeed, true);

    handler.postDelayed(() -> chooseLoadMethod(true), 700);
  }

  @Override public void onAccountLoaded(AccountRealm account) {
    currentUserId = account.getId();
  }

  @NonNull @Override public FeedGlobalPresenter createPresenter() {
    return new FeedGlobalPresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()
        ),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onPostOptionsClick(View v, EpoxyModel<?> model, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    final boolean self = currentUserId.equals(post.getOwnerId());
    menu.getMenu().getItem(0).setVisible(viewType != VIEW_TYPE_BOOKMARKED);
    menu.getMenu().getItem(1).setVisible(viewType == VIEW_TYPE_BOOKMARKED);
    menu.getMenu().getItem(2).setVisible(self);
    menu.getMenu().getItem(3).setVisible(self);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_feed_popup_bookmark:
          bookmarkPost(post);
          return true;
        case R.id.action_feed_popup_unbookmark:
          unbookmarkPost(model, post);
          return true;
        case R.id.action_feed_popup_edit:
          return true;
        case R.id.action_feed_popup_delete:
          deletePost(model, post);
          return true;
        default:
          return false;
      }
    });
  }

  @Override public void onCommentClick(View v, PostRealm post) {
    CommentController controller = CommentController.create(post);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onContentImageClick(View v, String url) {
    startTransaction(PhotoController.create(url), new FadeChangeHandler());
  }

  @Override public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onReadMoreClick(View v, PostRealm post) {
    PostDetailController controller =
        PostDetailController.create(post.getId(), post.getAcceptedCommentId());
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new HorizontalChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override public void onPostUpdated(PostRealm post) {
    modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
  }

  @Override public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      adapter.updatePost(action, (PostRealm) payload);
    }
  }

  private void setupRecyclerView() {
    adapter = FeedGlobalAdapter.builder(getActivity())
        .onProfileClickListener(this)
        .onReadMoreClickListener(this)
        .onOptionsClickListener(this)
        .onContentImageClickListener(this)
        .onCommentClickListener(this)
        .onShareClickListener(this)
        .onVoteClickListener(this)
        .build();

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvPostFeed.setLayoutManager(lm);
    rvPostFeed.addItemDecoration(new SpaceItemDecoration(12, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvPostFeed.setItemAnimator(animator);

    rvPostFeed.setHasFixedSize(true);
    rvPostFeed.setAdapter(adapter);

    rvPostFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
      @Override public void onLoadMore() {
        handler.postDelayed(() -> chooseLoadMethod(true), 700);
      }
    });
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setEnabled(viewType != VIEW_TYPE_TAGS);
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();

    if (ab != null) {
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
  }

  private void reloadQuestions(@NonNull MenuItem item, PostSorter sorter) {
    item.setChecked(true);
    adapter.clear();
    getPresenter().loadPostsByCategory(false, categoryName, sorter, 20);
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

  private void chooseLoadMethod(boolean pullToRefresh) {
    if (viewType == VIEW_TYPE_USER) {
      getPresenter().loadPostsByUser(pullToRefresh, userId, isCommented, 20);
    } else if (viewType == VIEW_TYPE_TAGS) {
      getPresenter().loadPostsByTag(pullToRefresh, tagName, PostSorter.NEWEST, 20);
    } else if (viewType == VIEW_TYPE_CATEGORY) {
      getPresenter().loadPostsByCategory(pullToRefresh, categoryName, PostSorter.NEWEST, 20);
    } else if (viewType == VIEW_TYPE_BOUNTY) {
      getPresenter().loadPostsByBounty(pullToRefresh, 20);
    } else if (viewType == VIEW_TYPE_BOOKMARKED) {
      getPresenter().loadPostsByBookmarked(pullToRefresh, 20);
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

  private void deletePost(EpoxyModel<?> model, PostRealm post) {
    getPresenter().deletePost(post.getId());
    adapter.delete(model);
    modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, post);
  }

  private void unbookmarkPost(EpoxyModel<?> model, PostRealm post) {
    getPresenter().unBookmarkPost(post.getId());
    adapter.delete(model);
    Snackbar.make(getView(), R.string.label_feed_unbookmarked, Snackbar.LENGTH_SHORT).show();
  }

  private void bookmarkPost(PostRealm post) {
    getPresenter().bookmarkPost(post.getId());
    Snackbar.make(getView(), R.string.label_feed_bookmarked, Snackbar.LENGTH_SHORT).show();
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }
}
