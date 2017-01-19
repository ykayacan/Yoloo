package com.yoloo.android.feature.comment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.ui.AutoCompleteMentionAdapter;
import com.yoloo.android.feature.ui.SpaceTokenizer;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.widget.DelayedMultiAutoCompleteTextView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.WeakHandler;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class CommentController extends MvpController<CommentView, CommentPresenter>
    implements CommentView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnVoteClickListener, OnMentionClickListener,
    AutoCompleteMentionAdapter.OnMentionFilterListener {

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_ACCEPTED_COMMENT_ID = "ACCEPTED_COMMENT_ID";
  private static final String KEY_COMMENT_COUNT = "COMMENT_COUNT";

  @BindView(R.id.rv_comment)
  RecyclerView rvComment;

  @BindView(R.id.swipe_comment)
  SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.tv_comment_write_comment)
  DelayedMultiAutoCompleteTextView tvWriteComment;

  @BindView(R.id.tv_comment_count)
  TextView tvCommentCount;

  @BindColor(R.color.primary)
  int primaryColor;

  private CommentAdapter adapter;

  private AutoCompleteMentionAdapter mentionAdapter;
  private WeakHandler handler = new WeakHandler();
  private Runnable mentionDropdownRunnable = () -> tvWriteComment.showDropDown();

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String postId;
  private String acceptedCommentId;
  private long commentCount;

  private String cursor;
  private String eTag;

  public CommentController(@Nullable Bundle args) {
    super(args);
  }

  public static CommentController create(String postId, String acceptedCommentId,
      long commentCount) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_POST_ID, postId)
        .putString(KEY_ACCEPTED_COMMENT_ID, acceptedCommentId)
        .putLong(KEY_COMMENT_COUNT, commentCount)
        .build();

    final CommentController controller = new CommentController(bundle);
    return controller;
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_comment, container, false);
  }

  @Override
  protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupPullToRefresh();
    setupRecyclerView();
    setupMentionsAdapter();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    final Bundle bundle = getArgs();

    postId = bundle.getString(KEY_POST_ID);
    acceptedCommentId = bundle.getString(KEY_ACCEPTED_COMMENT_ID);
    commentCount = bundle.getLong(KEY_COMMENT_COUNT);

    String quantityText =
        getResources().getQuantityString(R.plurals.label_comment_text, (int) commentCount,
            commentCount);

    tvCommentCount.setText(CountUtil.format(commentCount) + " " + quantityText);

    getPresenter().loadComments(false, postId, cursor, eTag, 20);
    getPresenter().loadComment(acceptedCommentId, true);

    rvComment.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvComment.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getActivity(), tvWriteComment);
    return super.handleBack();
  }

  @NonNull
  @Override
  public CommentPresenter createPresenter() {
    return new CommentPresenter(
        CommentRepository.getInstance(
            CommentRemoteDataStore.getInstance(),
            CommentDiskDataStore.getInstance()
        ),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override
  public void onLoaded(Response<List<CommentRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addComments(value.getData());
  }

  @Override
  public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onEmpty() {

  }

  @Override
  public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadComments(true, postId, cursor, eTag, 20);
    getPresenter().loadComment(acceptedCommentId, true);
  }

  @Override
  public void onCommentLoaded(CommentRealm comment) {
    adapter.addComment(comment, false);
    rvComment.smoothScrollToPosition(adapter.getItemCount() - 1);
  }

  @Override
  public void onAcceptedCommentLoaded(CommentRealm comment) {
    adapter.addComment(comment, true);
  }

  @Override
  public void onMentionSuggestionsLoaded(List<AccountRealm> suggestions) {
    mentionAdapter.setItems(suggestions);
    handler.post(mentionDropdownRunnable);
  }

  @Override
  public void onMentionClick(String value) {
    Snackbar.make(getView(), value, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onProfileClick(View v, String ownerId) {

  }

  @Override
  public void onVoteClick(String votableId, int direction, @VotableType int type) {
    getPresenter().voteComment(votableId, direction);
  }

  @Override
  public void onMentionFilter(String filtered) {
    getPresenter().suggestUser(filtered);
  }

  @Override
  public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @OnClick(R.id.btn_comment_send_comment)
  void sendComment() {
    final String content = tvWriteComment.getText().toString();

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
    adapter = new CommentAdapter(this, this, this);

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
}
