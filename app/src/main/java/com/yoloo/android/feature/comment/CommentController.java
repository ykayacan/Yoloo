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
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
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
import com.yoloo.android.util.KeyboardUtil;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    OnProfileClickListener, OnVoteClickListener, OnMentionClickListener,
    OnMarkAsAcceptedClickListener, OnItemLongClickListener<CommentRealm>,
    CommentAutocomplete.NewCommentListener {

  private static final String KEY_POST = "POST";

  @BindView(R.id.rv_comment) RecyclerView rvComment;
  @BindView(R.id.layout_compose) CommentAutocomplete composeLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private CommentEpoxyController epoxyController;

  private PostRealm post;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

  public CommentController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static CommentController create(PostRealm post) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_POST, Parcels.wrap(post)).build();

    return new CommentController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    final Bundle args = getArgs();
    post = Parcels.unwrap(args.getParcelable(KEY_POST));

    composeLayout.setPostId(post.getId());
    composeLayout.setNewCommentListener(this);

    setupToolbar();
    setupRecyclerView();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadData(post.getId(), post.getAcceptedCommentId());
      reEnter = true;
    }
  }

  @Override
  public boolean handleBack() {
    composeLayout.hideKeyboard();
    return super.handleBack();
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<CommentRealm> comments) {
    epoxyController.setData(Stream
        .of(comments)
        .map(comment -> comment
            .setPostAccepted(!TextUtils.isEmpty(post.getAcceptedCommentId()))
            .setPostOwner(post.getOwnerId().equals(comment.getOwnerId())))
        .toList(), false);
  }

  @Override
  public void onNewAccept(String commentId) {
    post.setAcceptedCommentId(commentId);

    modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
  }

  @Override
  public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override
  public void onEmpty() {

  }

  @Override
  public void onNewComment(CommentRealm comment) {
    epoxyController.addComment(comment
        .setOwner(true)
        .setPostAccepted(!TextUtils.isEmpty(post.getAcceptedCommentId()))
        .setPostOwner(post.getOwnerId().equals(comment.getOwnerId())));

    epoxyController.scrollToEnd(rvComment);

    modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
  }

  @Override
  public void onCommentDeleted() {

  }

  @NonNull
  @Override
  public CommentPresenter createPresenter() {
    return new CommentPresenter(CommentRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onMentionClick(String username) {
    Snackbar.make(getView(), username, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onProfileClick(View v, String userId) {
    KeyboardUtil.hideKeyboard(getView());
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override
  public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().voteComment(votableId, direction);
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
            epoxyController.delete(item);
          }
        })
        .show();
  }

  @Override
  public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }

  private void setupRecyclerView() {
    epoxyController =
        new CommentEpoxyController(getActivity(), post.getPostType(), Glide.with(getActivity()));
    epoxyController.setOnCommentLongClickListener(this);
    epoxyController.setOnMarkAsAcceptedClickListener(this);
    epoxyController.setOnMentionClickListener(this);
    epoxyController.setOnProfileClickListener(this);
    epoxyController.setOnVoteClickListener(this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    rvComment.setLayoutManager(lm);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvComment.setItemAnimator(animator);

    rvComment.setHasFixedSize(true);
    rvComment.setAdapter(epoxyController.getAdapter());
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
  }
}
