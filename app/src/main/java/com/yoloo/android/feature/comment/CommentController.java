package com.yoloo.android.feature.comment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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
import butterknife.OnClick;
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
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.feature.feed.common.event.AcceptedEvent;
import com.yoloo.android.feature.feed.common.event.UpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.widget.AutoCompleteMentionAdapter;
import com.yoloo.android.feature.ui.widget.DelayedMultiAutoCompleteTextView;
import com.yoloo.android.feature.ui.widget.SpaceTokenizer;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.RxBus;
import com.yoloo.android.util.WeakHandler;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnVoteClickListener, OnMentionClickListener, AutoCompleteMentionAdapter.OnMentionFilterListener,
    OnMarkAsAcceptedClickListener, OnCommentLongClickListener {

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_POST_OWNER_ID = "POST_OWNER_ID";
  private static final String KEY_ACCEPTED_COMMENT_ID = "ACCEPTED_COMMENT_ID";
  private static final String KEY_POST_TYPE = "POST_TYPE";

  @BindView(R.id.rv_comment) RecyclerView rvComment;
  @BindView(R.id.swipe_comment) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.tv_comment_write_comment) DelayedMultiAutoCompleteTextView tvWriteComment;
  @BindView(R.id.toolbar_comment) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;

  private CommentAdapter adapter;

  private AutoCompleteMentionAdapter mentionAdapter;
  private WeakHandler handler;

  private Runnable mentionDropdownRunnable = () -> tvWriteComment.showDropDown();

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String cursor;
  private String eTag;

  private String postId;
  private String postOwnerId;
  private String acceptedCommentId;
  private int postType;

  private boolean reEnter;

  public CommentController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static CommentController create(String postId, String postOwnerId,
      String acceptedCommentId, @PostType int postType) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_POST_ID, postId)
        .putString(KEY_POST_OWNER_ID, postOwnerId)
        .putString(KEY_ACCEPTED_COMMENT_ID, acceptedCommentId)
        .putInt(KEY_POST_TYPE, postType)
        .build();

    return new CommentController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    handler = new WeakHandler();

    final Bundle args = getArgs();
    postId = args.getString(KEY_POST_ID);
    postOwnerId = args.getString(KEY_POST_OWNER_ID);
    acceptedCommentId = args.getString(KEY_ACCEPTED_COMMENT_ID);
    postType = args.getInt(KEY_POST_TYPE);

    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
    setupMentionsAdapter();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadData(postId, postOwnerId, acceptedCommentId);
      reEnter = true;
    }

    rvComment.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvComment.removeOnScrollListener(endlessRecyclerViewScrollListener);
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
    KeyboardUtil.hideKeyboard(tvWriteComment);
    return super.handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(Response<List<CommentRealm>> value) {
    // empty
  }

  @Override
  public void onCommentsLoaded(Response<List<CommentRealm>> value, String currentUserId,
      boolean postOwner, boolean accepted) {
    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addComments(value.getData(), currentUserId, postOwner, accepted);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onAcceptedCommentLoaded(CommentRealm comment, boolean postOwner) {
    adapter.addAcceptedComment(comment, postOwner);
  }

  @Override
  public void onNewCommentLoaded(CommentRealm comment, boolean postOwner) {
    adapter.addComment(comment, postOwner);
    adapter.scrollToEnd(rvComment);

    RxBus.get().sendEvent(new UpdateEvent(new PostRealm().setId(postId)),
        ControllerUtil.getPreviousControllerClass(this));
  }

  @Override public void onNewAccept(String commentId) {
    acceptedCommentId = commentId;

    RxBus.get()
        .sendEvent(new AcceptedEvent(new PostRealm().setId(postId).setAcceptedCommentId(commentId)),
            ControllerUtil.getPreviousControllerClass(this));
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onEmpty() {

  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadData(postId, postOwnerId, acceptedCommentId);
  }

  @Override public void onMentionSuggestionsLoaded(List<AccountRealm> suggestions) {
    mentionAdapter.setItems(suggestions);
    handler.post(mentionDropdownRunnable);
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

  @Override public void onMentionClick(String value) {
    Snackbar.make(getView(), value, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onProfileClick(View v, String ownerId) {
    KeyboardUtil.hideKeyboard(tvWriteComment);
    startTransaction(ProfileController.create(ownerId), new VerticalChangeHandler());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().voteComment(votableId, direction);
  }

  @Override public void onMarkAsAccepted(View v, CommentRealm comment) {
    Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT).show();
    getPresenter().acceptComment(comment);
  }

  @Override public void onCommentLongClick(View v, EpoxyModel<?> model, CommentRealm comment) {
    new AlertDialog.Builder(getActivity())
        .setItems(R.array.action_comment_dialog, (dialog, which) -> {
          switch (which) {
            case 0:
              getPresenter().deleteComment(comment);
              adapter.delete(model);
              break;
            default:
              break;
          }
        })
        .show();
  }

  @Override public void onMentionFilter(String query) {
    getPresenter().suggestUser(query);
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @OnClick(R.id.btn_comment_send_comment) void sendComment() {
    final String content = tvWriteComment.getText().toString().trim();

    if (TextUtils.isEmpty(content)) {
      return;
    }

    CommentRealm comment = new CommentRealm()
        .setId(UUID.randomUUID().toString())
        .setContent(content)
        .setCreated(new Date())
        .setPostId(postId);

    getPresenter().sendComment(comment);

    tvWriteComment.setText("");
  }

  private void setupRecyclerView() {
    adapter = new CommentAdapter(this, this, this, this, this, postType);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvComment.setLayoutManager(layoutManager);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvComment.setItemAnimator(animator);

    rvComment.setHasFixedSize(true);
    rvComment.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupMentionsAdapter() {
    mentionAdapter = new AutoCompleteMentionAdapter(getActivity(), this);
    tvWriteComment.setAdapter(mentionAdapter);
    tvWriteComment.setTokenizer(new SpaceTokenizer());
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
