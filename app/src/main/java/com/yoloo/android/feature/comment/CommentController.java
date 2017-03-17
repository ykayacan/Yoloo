package com.yoloo.android.feature.comment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentAutocomplete;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.BundleBuilder;
import java.util.List;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    OnProfileClickListener, OnVoteClickListener, OnMentionClickListener,
    OnMarkAsAcceptedClickListener, OnItemLongClickListener<CommentRealm>,
    CommentAutocomplete.NewCommentListener {

  private static final String KEY_POST = "POST";

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_POST_OWNER_ID = "POST_OWNER_ID";
  private static final String KEY_ACCEPTED_COMMENT_ID = "ACCEPTED_COMMENT_ID";
  private static final String KEY_POST_TYPE = "POST_TYPE";

  @BindView(R.id.rv_comment) RecyclerView rvComment;
  @BindView(R.id.layout_compose) CommentAutocomplete composeLayout;
  @BindView(R.id.toolbar_comment) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;

  private CommentAdapter adapter;

  private AccountRealm account;
  private PostRealm post;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

  public CommentController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static CommentController create(PostRealm post) {
    final Bundle bundle = new BundleBuilder()
        .putParcelable(KEY_POST, post)
        .putString(KEY_POST_ID, post.getId())
        .putString(KEY_POST_OWNER_ID, post.getOwnerId())
        .putString(KEY_ACCEPTED_COMMENT_ID, post.getAcceptedCommentId())
        .putInt(KEY_POST_TYPE, post.getPostType())
        .build();

    return new CommentController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    final Bundle args = getArgs();
    post = args.getParcelable(KEY_POST);

    composeLayout.setPostId(post.getId());
    composeLayout.setNewCommentListener(this);

    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadData(post.getId(), post.getAcceptedCommentId());
      reEnter = true;
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public boolean handleBack() {
    composeLayout.hideKeyboard();
    return super.handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      //LceAnimator.showLoading(loadingView, swipeRefreshLayout, errorView);
    }
  }

  @Override public void onLoaded(List<CommentRealm> comments) {
    adapter.addComments(comments, account, post.getOwnerId(),
        !TextUtils.isEmpty(post.getAcceptedCommentId()));
  }

  @Override public void onAccountLoaded(AccountRealm account) {
    this.account = account;
  }

  @Override public void onAcceptedCommentLoaded(CommentRealm comment) {
    adapter.addAcceptedComment(comment, account, post.getOwnerId(),
        !TextUtils.isEmpty(post.getAcceptedCommentId()));
  }

  @Override public void onNewAccept(String commentId) {
    post.setAcceptedCommentId(commentId);

    modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override public void onEmpty() {

  }

  @Override public void onNewComment(CommentRealm comment) {
    adapter.addComment(comment, account, post.getOwnerId(),
        !TextUtils.isEmpty(post.getAcceptedCommentId()));
    adapter.scrollToEnd(rvComment);

    modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
  }

  @Override public void onCommentDeleted() {

  }

  @NonNull @Override public CommentPresenter createPresenter() {
    return new CommentPresenter(
        CommentRepository.getInstance(
            CommentRemoteDataStore.getInstance(),
            CommentDiskDataStore.getInstance()),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onMentionClick(String username) {
    Snackbar.make(getView(), username, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    composeLayout.hideKeyboard();
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().voteComment(votableId, direction);
  }

  @Override public void onMarkAsAccepted(View v, CommentRealm comment) {
    Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT).show();
    getPresenter().acceptComment(comment);
  }

  @Override public void onItemLongClick(View v, EpoxyModel<?> model, CommentRealm item) {
    new AlertDialog.Builder(getActivity())
        .setItems(R.array.action_comment_dialog, (dialog, which) -> {
          if (which == 0) {
            getPresenter().deleteComment(item);
            adapter.delete(model);
          }
        })
        .show();
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }

  private void setupRecyclerView() {
    adapter = new CommentAdapter(getActivity(), this, this, this, this, this, post.getPostType());

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    rvComment.setLayoutManager(lm);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvComment.setItemAnimator(animator);

    rvComment.setHasFixedSize(true);
    rvComment.setAdapter(adapter);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
