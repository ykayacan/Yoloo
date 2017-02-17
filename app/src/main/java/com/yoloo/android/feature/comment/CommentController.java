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
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.commentcompose.ComposeLayout;
import com.yoloo.android.feature.feed.common.event.AcceptedEvent;
import com.yoloo.android.feature.feed.common.event.UpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.RxBus;
import java.util.List;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    OnProfileClickListener, OnVoteClickListener, OnMentionClickListener,
    OnMarkAsAcceptedClickListener, OnItemLongClickListener<CommentRealm>,
    ComposeLayout.NewCommentListener {

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_POST_OWNER_ID = "POST_OWNER_ID";
  private static final String KEY_ACCEPTED_COMMENT_ID = "ACCEPTED_COMMENT_ID";
  private static final String KEY_POST_TYPE = "POST_TYPE";

  @BindView(R.id.rv_comment) RecyclerView rvComment;
  @BindView(R.id.layout_compose) ComposeLayout composeLayout;
  @BindView(R.id.toolbar_comment) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;

  private CommentAdapter adapter;

  private String cursor;
  private String eTag;

  private String postId;
  private String postOwnerId;
  private String acceptedCommentId;
  private int postType;

  private AccountRealm account;

  private boolean reEnter;

  public CommentController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static CommentController create(PostRealm post) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_POST_ID, post.getId())
        .putString(KEY_POST_OWNER_ID, post.getOwnerId())
        .putString(KEY_ACCEPTED_COMMENT_ID, post.getAcceptedCommentId())
        .putInt(KEY_POST_TYPE, post.getType())
        .build();

    return new CommentController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    final Bundle args = getArgs();
    postId = args.getString(KEY_POST_ID);
    postOwnerId = args.getString(KEY_POST_OWNER_ID);
    acceptedCommentId = args.getString(KEY_ACCEPTED_COMMENT_ID);
    postType = args.getInt(KEY_POST_TYPE);

    composeLayout.setPostId(postId);
    composeLayout.setNewCommentListener(this);

    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadData(postId, acceptedCommentId);
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

  @Override public void onLoaded(Response<List<CommentRealm>> value) {
    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addComments(value.getData(), account, postOwnerId,
        !TextUtils.isEmpty(acceptedCommentId));
  }

  @Override public void onAccountLoaded(AccountRealm account) {
    this.account = account;
  }

  @Override public void onAcceptedCommentLoaded(CommentRealm comment) {
    adapter.addAcceptedComment(comment, account, postOwnerId,
        !TextUtils.isEmpty(acceptedCommentId));
  }

  @Override public void onNewAccept(String commentId) {
    acceptedCommentId = commentId;

    RxBus.get()
        .sendEvent(new AcceptedEvent(new PostRealm().setId(postId).setAcceptedCommentId(commentId)),
            ControllerUtil.getPreviousControllerClass(this));
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override public void onEmpty() {

  }

  @Override public void onNewComment(CommentRealm comment) {
    adapter.addComment(comment, account, postOwnerId, !TextUtils.isEmpty(acceptedCommentId));
    adapter.scrollToEnd(rvComment);

    RxBus.get().sendEvent(new UpdateEvent(new PostRealm().setId(postId)),
        ControllerUtil.getPreviousControllerClass(this));
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

  @Override public void onMentionClick(String mention) {
    Snackbar.make(getView(), mention, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onProfileClick(View v, String ownerId) {
    composeLayout.hideKeyboard();
    startTransaction(ProfileController.create(ownerId), new VerticalChangeHandler());
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
          switch (which) {
            case 0:
              getPresenter().deleteComment(item);
              adapter.delete(model);
              break;
            default:
              break;
          }
        })
        .show();
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  private void setupRecyclerView() {
    adapter = new CommentAdapter(getActivity(), this, this, this, this, this, postType);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    lm.setInitialPrefetchItemCount(8);
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
