package com.yoloo.android.feature.postdetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentInput;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.InsetDividerDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.NetworkUtil;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import java.util.List;
import timber.log.Timber;

public class PostDetailController extends MvpController<PostDetailView, PostDetailPresenter>
    implements PostDetailView, SwipeRefreshLayout.OnRefreshListener, PostCallbacks,
    CommentCallbacks, CommentInput.NewCommentListener {

  private static final String KEY_POST_ID = "POST_ID";

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.layout_input) CommentInput input;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.divider) int dividerColor;

  private PostDetailEpoxyController epoxyController;

  private String postId;

  private PostRealm post;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

  private KeyboardUtil.SoftKeyboardToggleListener keyboardToggleListener;

  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  private boolean accepted;

  public PostDetailController(@Nullable Bundle args) {
    super(args);
  }

  public static PostDetailController create(@NonNull String postId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_POST_ID, postId).build();

    return new PostDetailController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_post_detail, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);

    final Bundle args = getArgs();
    postId = args.getString(KEY_POST_ID);

    setupPullToRefresh();
    setupToolbar();
    setupRecyclerView();

    input.setPostId(postId);
    input.setNewCommentListener(this);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);

    if (!reEnter) {
      getPresenter().loadData(false, postId);
      reEnter = true;
    }

    keyboardToggleListener = isVisible -> {
      if (isVisible) {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
      }
    };

    KeyboardUtil.addKeyboardToggleListener(getActivity(), keyboardToggleListener);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    KeyboardUtil.removeKeyboardToggleListener(keyboardToggleListener);
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    // empty
  }

  @Override
  public void onLoaded(List<FeedItem<?>> value) {
    epoxyController.setData(value, false);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onPostLoaded(PostRealm post) {
    this.post = post;
    this.accepted = post.getAcceptedCommentId() != null;
  }

  @Override
  public void onPostUpdated(PostRealm post) {
    this.post = post;
    this.accepted = post.getAcceptedCommentId() != null;

    epoxyController.updatePost(post);
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
    }
  }

  @Override
  public void onCommentUpdated(CommentRealm comment) {
    this.accepted = comment.isPostAccepted();
    epoxyController.updateComment(comment);
  }

  @Override public void onMoreLoaded(List<FeedItem<?>> items) {
    if (items.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(items);
    }
  }

  @Override
  public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
    Timber.e(e);
  }

  @Override
  public void onEmpty() {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onRefresh() {
    endlessRecyclerOnScrollListener.resetState();
    getPresenter().loadData(true, postId);
  }

  @NonNull
  @Override
  public PostDetailPresenter createPresenter() {
    return new PostDetailPresenter(CommentRepositoryProvider.getRepository(),
        PostRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  private void deletePost(PostRealm post) {
    getPresenter().deletePost(post.getId());
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, post);
    }
    getRouter().handleBack();
  }

  @Override
  public void onNewComment(CommentRealm comment) {
    epoxyController.addComment(comment.setOwner(true)
        .setPostType(post.getPostType())
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.getOwnerId().equals(comment.getOwnerId())));

    epoxyController.scrollToEnd(recyclerView);
  }

  private void setupRecyclerView() {
    epoxyController = new PostDetailEpoxyController(Glide.with(getActivity()));
    epoxyController.setPostCallbacks(this);
    epoxyController.setCommentCallbacks(this);

    LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(lm);
    recyclerView.setItemAnimator(new SlideInItemAnimator());

    recyclerView.addItemDecoration(new InsetDividerDecoration(R.layout.item_comment,
        getResources().getDimensionPixelSize(R.dimen.divider_height),
        getResources().getDimensionPixelSize(R.dimen.keyline_1), dividerColor));

    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(epoxyController.getAdapter());

    endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(lm) {
          @Override public void onLoadMore(int totalItemsCount, RecyclerView view) {
            getPresenter().loadMoreComments();
            epoxyController.showLoader();
          }
        };

    recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }

  @Override public void onPostClickListener(@NonNull PostRealm post) {
    // empty
  }

  @Override public void onPostContentImageClickListener(@NonNull MediaRealm media) {
    startTransaction(FullscreenPhotoController.create(media.getLargeSizeUrl()),
        new FadeChangeHandler());
  }

  @Override public void onPostProfileClickListener(@NonNull String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onPostBookmarkClickListener(@NonNull PostRealm post) {
    if (post.isBookmarked()) {
      getPresenter().unBookmarkPost(post.getId());
    } else {
      getPresenter().bookmarkPost(post.getId());
    }
  }

  @Override public void onPostOptionsClickListener(View v, @NonNull PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        if (NetworkUtil.isNetworkAvailable(getActivity())) {
          deletePost(post);
          return true;
        } else {
          Snackbar.make(getView(), R.string.all_network_required_delete,
              Snackbar.LENGTH_SHORT).show();
          return false;
        }
      }

      return super.onOptionsItemSelected(item);
    });
  }

  @Override public void onPostShareClickListener(@NonNull PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onPostCommentClickListener(@NonNull PostRealm post) {
    input.showKeyboard();
  }

  @Override public void onPostVoteClickListener(@NonNull PostRealm post, int direction) {
    getPresenter().votePost(post.getId(), direction);
  }

  @Override public void onPostTagClickListener(@NonNull String tagName) {
    startTransaction(PostListController.ofTag(tagName), new VerticalChangeHandler());
  }

  @Override public void onCommentLongClickListener(@NonNull CommentRealm comment) {
    new AlertDialog.Builder(getActivity())
        .setItems(R.array.action_comment_dialog, (dialog, which) -> {
          if (which == 0) {
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
              getPresenter().deleteComment(comment);
              epoxyController.deleteComment(comment);
            } else {
              Snackbar.make(getView(), R.string.all_network_required_delete, Snackbar.LENGTH_SHORT)
                  .show();
            }
          }
        })
        .show();
  }

  @Override public void onCommentProfileClickListener(@NonNull String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onCommentMentionClickListener(@NonNull String username) {

  }

  @Override public void onCommentVoteClickListener(@NonNull CommentRealm comment, int direction) {
    getPresenter().voteComment(comment.getId(), direction);
  }

  @Override public void onCommentAcceptRequestClickListener(@NonNull CommentRealm comment) {
    if (accepted) {
      Snackbar.make(getView(), R.string.comments_accepted_error, Snackbar.LENGTH_SHORT).show();
    } else {
      Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT)
          .show();
      getPresenter().acceptComment(comment);
    }
  }
}
