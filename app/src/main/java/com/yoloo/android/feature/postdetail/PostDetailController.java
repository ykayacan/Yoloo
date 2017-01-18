package com.yoloo.android.feature.postdetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.ui.AutoCompleteMentionAdapter;
import com.yoloo.android.feature.ui.SpaceTokenizer;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.widget.DelayedMultiAutoCompleteTextView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.WeakHandler;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class PostDetailController extends MvpController<PostDetailView, PostDetailPresenter>
    implements PostDetailView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnProfileClickListener,
    OnOptionsClickListener, OnShareClickListener, OnCommentClickListener,
    FeedAdapter.OnCategoryClickListener, OnVoteClickListener, OnContentImageClickListener,
    OnMentionClickListener, AutoCompleteMentionAdapter.OnMentionFilterListener {

  private static final String KEY_POST_ID = "POST_ID";
  private static final String KEY_ACCEPTED_COMMENT_ID = "ACCEPTED_COMMENT_ID";
  private static final String KEY_MODEL_ID = "MODEL_ID";

  @BindView(R.id.toolbar_post_detail) Toolbar toolbar;

  @BindView(R.id.rv_post_detail) RecyclerView rvFeed;

  @BindView(R.id.swipe_post_detail) SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.tv_post_detail_write_comment) DelayedMultiAutoCompleteTextView tvWriteComment;

  @BindColor(R.color.primary) int primaryColor;

  private PostDetailAdapter adapter;

  private AutoCompleteMentionAdapter mentionAdapter;
  private WeakHandler handler = new WeakHandler();
  private Runnable mentionDropdownRunnable = () -> tvWriteComment.showDropDown();

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String postId;
  private String acceptedCommentId;
  private long modelId;

  private String cursor;
  private String eTag;

  private Object payload;

  public PostDetailController(@Nullable Bundle args) {
    super(args);
  }

  public static <T extends Controller & OnModelUpdateListener> PostDetailController create(
      String postId, String acceptedCommentId, long modelId, T targetController) {
    final Bundle bundle = new BundleBuilder().putString(KEY_POST_ID, postId)
        .putString(KEY_ACCEPTED_COMMENT_ID, acceptedCommentId)
        .putLong(KEY_MODEL_ID, modelId)
        .build();

    final PostDetailController controller = new PostDetailController(bundle);
    controller.setTargetController(targetController);

    return controller;
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_post_detail, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
    setupMentionsAdapter();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    final Bundle bundle = getArgs();

    postId = bundle.getString(KEY_POST_ID);
    modelId = bundle.getLong(KEY_MODEL_ID);
    acceptedCommentId = bundle.getString(KEY_ACCEPTED_COMMENT_ID);

    getPresenter().loadPost(postId);
    getPresenter().loadComments(false, postId, cursor, eTag, 20);
    getPresenter().loadComment(acceptedCommentId, true);
  }

  @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.clear();
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        notfiyPayload();
        KeyboardUtil.hideKeyboard(getActivity(), tvWriteComment);
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public boolean handleBack() {
    notfiyPayload();
    KeyboardUtil.hideKeyboard(getActivity(), tvWriteComment);
    return super.handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<CommentRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addComments(value.getData());
  }

  @Override public void onPostLoaded(PostRealm post) {
    swipeRefreshLayout.setRefreshing(false);

    adapter.addQuestion(post);
  }

  @Override public void onCommentLoaded(CommentRealm comment) {
    adapter.addComment(comment, false);
    rvFeed.smoothScrollToPosition(adapter.getItemCount() - 1);
  }

  @Override public void onAcceptedCommentLoaded(CommentRealm comment) {
    adapter.addComment(comment, true);
  }

  @Override public void onPostUpdated(PostRealm post) {
    payload = post;
  }

  @Override public void onMentionSuggestionsLoaded(List<AccountRealm> suggestions) {
    mentionAdapter.setItems(suggestions);
    handler.post(mentionDropdownRunnable);
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onEmpty() {

  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadComments(true, postId, cursor, eTag, 20);
    getPresenter().loadComment(acceptedCommentId, true);
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @NonNull @Override public PostDetailPresenter createPresenter() {
    return new PostDetailPresenter(
        CommentRepository.getInstance(CommentRemoteDataStore.getInstance(),
            CommentDiskDataStore.getInstance()),
        PostRepository.getInstance(PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()),
        UserRepository.getInstance(UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onCategoryClick(View v, String categoryId, String name) {

  }

  @Override public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    tvWriteComment.requestFocus();
    KeyboardUtil.showDelayedKeyboard(getActivity(), tvWriteComment);
  }

  @Override public void onOptionsClick(View v, EpoxyModel<?> model, String postId, boolean self) {
    final PopupMenu optionsMenu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    if (self) {
      optionsMenu.getMenu().getItem(1).setVisible(false);
      optionsMenu.getMenu().getItem(2).setVisible(false);
    }
    optionsMenu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_post_delete:
          getPresenter().deletePost(postId);
          adapter.delete(model);
          getRouter().popCurrentController();
          return true;
      }
      return false;
    });
  }

  @Override public void onContentImageClick(View v, String url) {

  }

  @Override public void onProfileClick(View v, String ownerId) {

  }

  @Override public void onShareClick(View v) {

  }

  @Override public void onVoteClick(String votableId, int direction, @VotableType int type) {
    if (type == VotableType.POST) {
      getPresenter().votePost(votableId, direction);
    } else {
      getPresenter().voteComment(votableId, direction);
    }
  }

  @Override public void onMentionClick(String value) {
    Snackbar.make(getView(), value, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onMentionFilter(String filtered) {
    getPresenter().suggestUser(filtered);
  }

  @OnClick(R.id.btn_post_detail_send_comment) void sendComment() {
    final String content = tvWriteComment.getText().toString();

    CommentRealm comment = new CommentRealm().setId(UUID.randomUUID().toString())
        .setContent(content)
        .setCreated(new Date())
        .setPostId(postId);

    getPresenter().sendComment(comment);

    tvWriteComment.setText("");
  }

  private void setupRecyclerView() {
    adapter = PostDetailAdapter.builder()
        .onProfileClickListener(this)
        .onCommentClickListener(this)
        .onContentImageClickListener(this)
        .onOptionsClickListener(this)
        .onVoteClickListener(this)
        .onShareClickListener(this)
        .onMentionClickListener(this)
        .build();

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(layoutManager);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
    rvFeed.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  private void setupMentionsAdapter() {
    mentionAdapter = new AutoCompleteMentionAdapter(getActivity(), this);
    tvWriteComment.setAdapter(mentionAdapter);
    tvWriteComment.setTokenizer(new SpaceTokenizer());
  }

  private void notfiyPayload() {
    Controller target = getTargetController();
    if (payload != null && target != null) {
      ((OnModelUpdateListener) target).onModelUpdate(modelId, payload);
    }
  }
}