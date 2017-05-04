package com.yoloo.android.feature.postlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.editor.PostEditorController;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import java.util.List;
import timber.log.Timber;

public class PostListController extends MvpController<PostListView, PostListPresenter>
    implements PostListView, SwipeRefreshLayout.OnRefreshListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnItemClickListener<PostRealm>, OnShareClickListener,
    OnCommentClickListener, OnVoteClickListener, OnContentImageClickListener, OnModelUpdateEvent,
    OnBookmarkClickListener {

  private static final int VIEW_TYPE_GROUP = 0;
  private static final int VIEW_TYPE_TAGS = 1;
  private static final int VIEW_TYPE_BOUNTY = 2;
  private static final int VIEW_TYPE_BOOKMARKED = 3;
  private static final int VIEW_TYPE_USER = 4;
  private static final int VIEW_TYPE_SORTER = 5;

  private static final String KEY_GROUP_ID = "GROUP_ID";
  private static final String KEY_TAG_NAME = "TAG_NAME";
  private static final String KEY_USER_ID = "USER_ID";
  private static final String KEY_BOUNTY = "BOUNTY";
  private static final String KEY_BOOKMARKED = "BOOKMARKED";
  private static final String KEY_POST_SORTER = "POST_SORTER";

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.rv_feed_global) RecyclerView rvPostFeed;
  @BindView(R.id.swipe_feed_global) SwipeRefreshLayout swipeRefreshLayout;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private PostListEpoxyController epoxyController;

  private int viewType;

  private String groupId;
  private String tagName;
  private String userId;
  private boolean isBounty;
  private boolean isBookmarked;
  private PostSorter postSorter;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

  public PostListController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static PostListController ofBounty() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOUNTY, true).build();

    return new PostListController(bundle);
  }

  public static PostListController ofBookmarked() {
    final Bundle bundle = new BundleBuilder().putBoolean(KEY_BOOKMARKED, true).build();

    return new PostListController(bundle);
  }

  public static PostListController ofGroup(@NonNull String groupId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_GROUP_ID, groupId).build();

    return new PostListController(bundle);
  }

  public static PostListController ofTag(@NonNull String tagName) {
    final Bundle bundle = new BundleBuilder().putString(KEY_TAG_NAME, tagName).build();

    return new PostListController(bundle);
  }

  public static PostListController ofUser(@NonNull String userId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_USER_ID, userId).build();

    return new PostListController(bundle);
  }

  public static PostListController ofPostSorter(PostSorter sorter) {
    final Bundle bundle = new BundleBuilder().putSerializable(KEY_POST_SORTER, sorter).build();

    return new PostListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_post_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    final Bundle args = getArgs();

    groupId = args.getString(KEY_GROUP_ID);
    tagName = args.getString(KEY_TAG_NAME);
    userId = args.getString(KEY_USER_ID);
    isBounty = args.getBoolean(KEY_BOUNTY, false);
    isBookmarked = args.getBoolean(KEY_BOOKMARKED, false);
    postSorter = (PostSorter) args.getSerializable(KEY_POST_SORTER);

    setViewType();

    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();

    rootView.setViewStateListener((stateView, viewState) -> {
      if (viewState == StateLayout.VIEW_STATE_EMPTY) {
        View emptyActionView = ButterKnife.findById(stateView, R.id.btn_empty_action);

        if (emptyActionView != null) {
          emptyActionView.setOnClickListener(v -> {
            Controller parentController = getParentController();
            if (parentController == null) {
              startTransaction(PostEditorController.create(), new VerticalChangeHandler());
            } else {
              VerticalChangeHandler handler = new VerticalChangeHandler();

              parentController
                  .getRouter()
                  .pushController(RouterTransaction
                      .with(PostEditorController.create())
                      .pushChangeHandler(handler)
                      .popChangeHandler(handler));
            }
          });
        }
      }
    });
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      chooseLoadMethod(false);
      reEnter = true;
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    getDrawerLayout().setFitsSystemWindows(false);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<FeedItem> value) {
    epoxyController.setData(value, false);
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
    if (viewType == VIEW_TYPE_BOOKMARKED) {
      rootView.setEmptyViewRes(R.layout.layout_bookmarks_empty_view);
    }
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onRefresh() {
    epoxyController.onRefresh();
    chooseLoadMethod(true);
  }

  @Override
  public void onAccountLoaded(AccountRealm account) {
    epoxyController.setUserId(account.getId());
    //adapter.setUserId(currentUserId);
  }

  @NonNull
  @Override
  public PostListPresenter createPresenter() {
    return new PostListPresenter(PostRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onPostOptionsClick(View v, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        deletePost(post);
        epoxyController.deletePost(post);
        return true;
      }

      return super.onOptionsItemSelected(item);
    });
  }

  @Override
  public void onCommentClick(View v, PostRealm post) {
    CommentController controller = CommentController.create(post);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override
  public void onContentImageClick(View v, MediaRealm media) {
    startTransaction(FullscreenPhotoController.create(media.getLargeSizeUrl(), media.getId()),
        new FadeChangeHandler());
  }

  @Override
  public void onProfileClick(View v, String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, PostRealm item) {
    PostDetailController controller =
        PostDetailController.create(item.getId(), item.getAcceptedCommentId());
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new HorizontalChangeHandler());
  }

  @Override
  public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override
  public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override
  public void onPostUpdated(PostRealm post) {
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
    }
  }

  @Override
  public void onBookmarkClick(@NonNull String postId, boolean bookmark) {
    if (bookmark) {
      getPresenter().bookmarkPost(postId);
    } else {
      getPresenter().unBookmarkPost(postId);
    }
  }

  @Override
  public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      //adapter.updatePost(action, (PostRealm) payload);
    }
  }

  private void setupRecyclerView() {
    epoxyController = new PostListEpoxyController(getActivity());
    epoxyController.setOnProfileClickListener(this);
    epoxyController.setOnPostClickListener(this);
    epoxyController.setOnPostOptionsClickListener(this);
    epoxyController.setOnContentImageClickListener(this);
    epoxyController.setOnCommentClickListener(this);
    epoxyController.setOnShareClickListener(this);
    epoxyController.setOnVoteClickListener(this);
    epoxyController.setOnBookmarkClickListener(this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvPostFeed.setLayoutManager(lm);
    rvPostFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvPostFeed.setItemAnimator(animator);

    rvPostFeed.setHasFixedSize(true);
    rvPostFeed.setAdapter(epoxyController.getAdapter());

    /*rvPostFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
      @Override
      public void onLoadMore() {
        handler.postDelayed(() -> chooseLoadMethod(true), 700);
      }
    });*/
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setEnabled(viewType != VIEW_TYPE_TAGS);
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);

    switch (viewType) {
      case VIEW_TYPE_USER:
        toolbar.setVisibility(View.GONE);
        break;
      case VIEW_TYPE_GROUP:
        toolbar.setVisibility(View.GONE);
        break;
      case VIEW_TYPE_TAGS:
        ab.setTitle(tagName);
        break;
      case VIEW_TYPE_BOUNTY:
        ab.setTitle(R.string.label_toolbar_bounty_title);
        break;
      case VIEW_TYPE_BOOKMARKED:
        ab.setTitle(R.string.label_toolbar_bookmark_title);
        break;
      case VIEW_TYPE_SORTER:
        if (postSorter == PostSorter.HOT) {
          ab.setTitle("Trending Posts");
        } else if (postSorter == PostSorter.NEWEST) {
          ab.setTitle("Newest Posts");
        }
        break;
    }
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    Controller parentController = getParentController();
    if (parentController == null) {
      getRouter().pushController(
          RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
    } else {
      parentController
          .getRouter()
          .pushController(
              RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
    }
  }

  private void chooseLoadMethod(boolean pullToRefresh) {
    switch (viewType) {
      case VIEW_TYPE_USER:
        getPresenter().loadPostsByUser(pullToRefresh, userId, 20);
        break;
      case VIEW_TYPE_TAGS:
        getPresenter().loadPostsByTag(pullToRefresh, tagName, PostSorter.NEWEST, 20);
        break;
      case VIEW_TYPE_GROUP:
        getPresenter().loadPostsByGroup(pullToRefresh, groupId, PostSorter.NEWEST, 20);
        break;
      case VIEW_TYPE_BOUNTY:
        getPresenter().loadPostsByBounty(pullToRefresh, 20);
        break;
      case VIEW_TYPE_BOOKMARKED:
        getPresenter().loadPostsByBookmarked(pullToRefresh, 20);
        break;
      case VIEW_TYPE_SORTER:
        getPresenter().loadPostsByPostSorter(pullToRefresh, postSorter, 20);
        break;
    }
  }

  private void setViewType() {
    if (!TextUtils.isEmpty(groupId)) {
      viewType = VIEW_TYPE_GROUP;
    } else if (!TextUtils.isEmpty(tagName)) {
      viewType = VIEW_TYPE_TAGS;
    } else if (!TextUtils.isEmpty(userId)) {
      viewType = VIEW_TYPE_USER;
    } else if (isBounty) {
      viewType = VIEW_TYPE_BOUNTY;
    } else if (isBookmarked) {
      viewType = VIEW_TYPE_BOOKMARKED;
    } else if (postSorter != null) {
      viewType = VIEW_TYPE_SORTER;
    }
  }

  private void deletePost(PostRealm post) {
    getPresenter().deletePost(post.getId());
    epoxyController.deletePost(post);
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, post);
    }
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }
}
