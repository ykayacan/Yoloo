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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentInput;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.NetworkUtil;
import java.util.List;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, CommentCallbacks, CommentInput.NewCommentListener {

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_POST_OWNER_ID = "POST_OWNER_ID";
  private static final String KEY_POST_TYPE = "POST_TYPE";
  private static final String KEY_HAS_ACCEPTED_COMMENT = "HAS_ACCEPTED_COMMENT";

  @BindView(R.id.rv_comment) RecyclerView rvComment;
  @BindView(R.id.layout_input) CommentInput composeLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private CommentEpoxyController epoxyController;

  private String postId;
  private String postOwnerId;
  private int postType;
  private boolean hasAcceptedComment;

  private boolean reEnter;

  private OnModelUpdateEvent modelUpdateEvent;

  private KeyboardUtil.SoftKeyboardToggleListener keyboardToggleListener;

  public CommentController(@Nullable Bundle args) {
    super(args);
  }

  public static CommentController create(@NonNull String postId, @NonNull String postOwnerId,
      int postType, boolean hasAcceptedComment) {
    final Bundle bundle = new BundleBuilder().putString(KEY_POST_ID, postId)
        .putString(KEY_POST_OWNER_ID, postOwnerId)
        .putInt(KEY_POST_TYPE, postType)
        .putBoolean(KEY_HAS_ACCEPTED_COMMENT, hasAcceptedComment)
        .build();

    return new CommentController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);

    final Bundle args = getArgs();
    postId = args.getString(KEY_POST_ID);
    postOwnerId = args.getString(KEY_POST_OWNER_ID);
    postType = args.getInt(KEY_POST_TYPE);
    hasAcceptedComment = args.getBoolean(KEY_HAS_ACCEPTED_COMMENT);

    composeLayout.setPostId(postId);
    composeLayout.setNewCommentListener(this);

    setupToolbar();
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadComments(false, false, postId, postOwnerId, hasAcceptedComment, postType);
      reEnter = true;
    }

    keyboardToggleListener = isVisible -> {
      if (isVisible) {
        rvComment.smoothScrollToPosition(rvComment.getAdapter().getItemCount());
      }
    };

    KeyboardUtil.addKeyboardToggleListener(getActivity(), keyboardToggleListener);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    KeyboardUtil.removeKeyboardToggleListener(keyboardToggleListener);
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {
    // empty
  }

  @Override public void onLoaded(List<CommentRealm> comments) {
    epoxyController.setData(comments, false);
  }

  @Override public void onMoreLoaded(List<CommentRealm> comments) {
    if (comments.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(comments);
    }
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override public void onEmpty() {

  }

  @Override public void onNewComment(CommentRealm comment) {
    epoxyController.addComment(comment.setOwner(true)
        .setPostType(postType)
        .setPostAccepted(hasAcceptedComment)
        .setPostOwner(postOwnerId.equals(comment.getOwnerId())));

    epoxyController.scrollToEnd(rvComment);

    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, postId);
    }
  }

  @NonNull @Override public CommentPresenter createPresenter() {
    return new CommentPresenter(CommentRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override public void onCommentUpdated(CommentRealm comment) {
    this.hasAcceptedComment = comment.isPostAccepted();
    epoxyController.updateComment(comment);
  }

  private void setupRecyclerView() {
    epoxyController =
        new CommentEpoxyController(Glide.with(getActivity()));
    epoxyController.setCommentCallbacks(this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    rvComment.setLayoutManager(lm);

    rvComment.setItemAnimator(new SlideInItemAnimator());

    rvComment.setHasFixedSize(true);
    rvComment.setAdapter(epoxyController.getAdapter());

    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(lm) {
          @Override public void onLoadMore(int totalItemsCount, RecyclerView view) {
            getPresenter().loadComments(false, true, postId, postOwnerId, hasAcceptedComment,
                postType);
            epoxyController.showLoader();
          }
        };

    rvComment.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setTitle(R.string.controller_comments_title);
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
    if (hasAcceptedComment) {
      Snackbar.make(getView(), R.string.comments_accepted_error, Snackbar.LENGTH_SHORT).show();
    } else {
      Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT)
          .show();
      getPresenter().acceptComment(comment);
    }
  }
}
