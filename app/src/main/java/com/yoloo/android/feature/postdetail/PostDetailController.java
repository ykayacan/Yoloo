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
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.FeedItem;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentAutocomplete;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.InsetDividerDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import java.util.List;
import timber.log.Timber;

public class PostDetailController extends MvpController<PostDetailView, PostDetailPresenter>
    implements PostDetailView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnShareClickListener, OnCommentClickListener, OnVoteClickListener,
    OnContentImageClickListener, CommentAutocomplete.NewCommentListener,
    OnMarkAsAcceptedClickListener, OnItemLongClickListener<CommentRealm>, OnMentionClickListener {

  private static final String KEY_POST_ID = "POST_ID";

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.rv_post_detail) RecyclerView rvFeed;
  @BindView(R.id.swipe_post_detail) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.layout_compose) CommentAutocomplete composeLayout;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.divider) int dividerColor;

  private PostDetailEpoxyController epoxyController;

  private String postId;

  private PostRealm post;
  private AccountRealm me;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

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

    composeLayout.setPostId(postId);
    composeLayout.setNewCommentListener(this);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadData(false, postId);
      reEnter = true;
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<FeedItem> value) {
    epoxyController.setData(value, false);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onPostLoaded(PostRealm post) {
    this.post = post;
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    this.me = me;
  }

  @Override
  public void onPostUpdated(PostRealm post) {
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
    }
  }

  @Override
  public void onCommentAccepted(String commentId) {

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
  public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @Override
  public void onRefresh() {
    getPresenter().loadData(true, postId);
  }

  @NonNull
  @Override
  public PostDetailPresenter createPresenter() {
    return new PostDetailPresenter(CommentRepositoryProvider.getRepository(),
        PostRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  @Override
  public void onCommentClick(View v, PostRealm post) {
    composeLayout.showKeyboard();
  }

  @Override
  public void onPostOptionsClick(View v, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        processPostDelete(post);
        return true;
      }

      return super.onOptionsItemSelected(item);
    });
  }

  private void processPostDelete(PostRealm post) {
    getPresenter().deletePost(post.getId());
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, post);
    }
    getRouter().handleBack();
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
  public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override
  public void onVoteClick(String votableId, int direction, @Type int type) {
    if (type == Type.POST) {
      getPresenter().votePost(votableId, direction);
    } else {
      getPresenter().voteComment(votableId, direction);
    }
  }

  @Override
  public void onNewComment(CommentRealm comment) {
    comment
        .setPostType(post.getPostType())
        .setOwner(me.getId().equals(comment.getOwnerId()))
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.getOwnerId().equals(me.getId()));

    epoxyController.addComment(comment);
    epoxyController.scrollToEnd(rvFeed);
  }

  @Override
  public void onMarkAsAccepted(View v, CommentRealm comment) {
    Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT).show();
    getPresenter().acceptComment(comment);
  }

  @Override
  public void onItemLongClick(View v, EpoxyModel<?> model, CommentRealm item) {
    new AlertDialog.Builder(getActivity())
        .setItems(R.array.action_comment_dialog, (dialog, which) -> {
          if (which == 0) {
            getPresenter().deleteComment(item);
            epoxyController.deleteComment(item);
          }
        })
        .show();
  }

  @Override
  public void onMentionClick(String username) {
    Timber.d("Mentions: %s", username);
  }

  private void setupRecyclerView() {
    epoxyController =
        new PostDetailEpoxyController(getActivity(), Glide.with(getActivity()));
    epoxyController.setOnProfileClickListener(this);

    epoxyController.setOnContentImageClickListener(this);
    epoxyController.setOnPostOptionsClickListener(this);

    epoxyController.setOnVoteClickListener(this);
    epoxyController.setOnShareClickListener(this);
    epoxyController.setOnCommentClickListener(this);

    epoxyController.setOnCommentLongClickListener(this);
    epoxyController.setOnMentionClickListener(this);

    epoxyController.setOnMarkAsAcceptedClickListener(this);

    rvFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvFeed.setItemAnimator(new SlideInItemAnimator());

    rvFeed.addItemDecoration(new InsetDividerDecoration(R.layout.item_comment,
        getResources().getDimensionPixelSize(R.dimen.divider_height),
        getResources().getDimensionPixelSize(R.dimen.keyline_1), dividerColor));

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(epoxyController.getAdapter());
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
}
