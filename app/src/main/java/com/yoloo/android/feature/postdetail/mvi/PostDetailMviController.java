package com.yoloo.android.feature.postdetail.mvi;

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
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.feature.base.BaseMviController;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentInput;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.InsetDividerDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.NetworkUtil;
import com.yoloo.android.util.Pair;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class PostDetailMviController
    extends BaseMviController<PostDetailMviView, PostDetailMviPresenter>
    implements PostDetailMviView, PostCallbacks, CommentCallbacks, CommentInput.NewCommentListener {

  private static final String KEY_POST_ID = "POST_ID";

  private final PublishSubject<Boolean> loadMoreSubject = PublishSubject.create();
  private final PublishSubject<CommentRealm> newCommentSubject = PublishSubject.create();
  private final PublishSubject<PostRealm> bookmarkSubject = PublishSubject.create();
  private final PublishSubject<PostRealm> deletePostSubject = PublishSubject.create();
  private final PublishSubject<CommentRealm> deleteCommentSubject = PublishSubject.create();
  private final PublishSubject<Pair<PostRealm, Integer>> votePostSubject = PublishSubject.create();
  private final PublishSubject<Pair<CommentRealm, Integer>> voteCommentSubject =
      PublishSubject.create();
  private final PublishSubject<CommentRealm> acceptCommentSubject = PublishSubject.create();

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.layout_input) CommentInput commentInput;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.divider) int dividerColor;

  private PostDetailMviEpoxyController epoxyController;

  private boolean restoringViewState = false;

  private String postId;

  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  private OnModelUpdateEvent modelUpdateEvent;

  public PostDetailMviController(@Nullable Bundle args) {
    super(args);
  }

  public static PostDetailMviController create(@NonNull String postId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_POST_ID, postId).build();

    return new PostDetailMviController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_post_detail, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);

    postId = getArgs().getString(KEY_POST_ID);

    setupPullToRefresh();
    setupToolbar();
    setupRecyclerView();

    commentInput.setPostId(postId);
    commentInput.setNewCommentListener(this);

    KeyboardUtil.addKeyboardToggleListener(getActivity(), isVisible -> {
      if (isVisible) {
        epoxyController.scrollToEnd(recyclerView);
      }
    });
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override protected void onDestroy() {
    KeyboardUtil.removeAllKeyboardToggleListeners();
    super.onDestroy();
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @Override public void onNewComment(CommentRealm comment) {
    newCommentSubject.onNext(comment);
    epoxyController.scrollToEnd(recyclerView);
  }

  private void setupRecyclerView() {
    epoxyController = new PostDetailMviEpoxyController(Glide.with(getActivity()));
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

    endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(lm) {
      @Override public void onLoadMore(int totalItemsCount, RecyclerView view) {
        loadMoreSubject.onNext(true);
      }
    };

    recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void setupPullToRefresh() {
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
    bookmarkSubject.onNext(post);
  }

  @Override public void onPostOptionsClickListener(View v, @NonNull PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        if (NetworkUtil.isNetworkAvailable(getActivity())) {
          deletePostSubject.onNext(post);
          return true;
        } else {
          Snackbar.make(getView(), R.string.all_network_required_delete, Snackbar.LENGTH_SHORT)
              .show();
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
    commentInput.showKeyboard();
  }

  @Override public void onPostVoteClickListener(@NonNull PostRealm post, int direction) {
    votePostSubject.onNext(Pair.create(post, direction));
  }

  @Override public void onPostTagClickListener(@NonNull String tagName) {
    startTransaction(PostListController.ofTag(tagName), new VerticalChangeHandler());
  }

  @Override public void onCommentLongClickListener(@NonNull CommentRealm comment) {
    new AlertDialog.Builder(getActivity()).setItems(R.array.action_comment_dialog,
        (dialog, which) -> {
          if (which == 0) {
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
              deleteCommentSubject.onNext(comment);
            } else {
              Snackbar.make(getView(), R.string.all_network_required_delete, Snackbar.LENGTH_SHORT)
                  .show();
            }
          }
        }).show();
  }

  @Override public void onCommentProfileClickListener(@NonNull String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onCommentMentionClickListener(@NonNull String username) {

  }

  @Override public void onCommentVoteClickListener(@NonNull CommentRealm comment, int direction) {
    voteCommentSubject.onNext(Pair.create(comment, direction));
  }

  @Override public void onCommentAcceptRequestClickListener(@NonNull CommentRealm comment) {
    acceptCommentSubject.onNext(comment);
  }

  @NonNull @Override public Observable<String> loadFirstPageIntent() {
    return Observable.just(postId)
        .filter(postId -> !restoringViewState)
        .doOnComplete(() -> Timber.d("firstPage completed"));
  }

  @NonNull @Override public Observable<Boolean> loadNextPageIntent() {
    return loadMoreSubject;
  }

  @NonNull @Override public Observable<String> pullToRefreshIntent() {
    return RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .map(ignored -> postId)
        .doOnSubscribe(disposable -> endlessRecyclerOnScrollListener.resetState());
  }

  @NonNull @Override public Observable<CommentRealm> newCommentIntent() {
    return newCommentSubject;
  }

  @NonNull @Override public Observable<PostRealm> bookmarkIntent() {
    return bookmarkSubject.doOnComplete(() -> Timber.d("bookmark completed"));
  }

  @NonNull @Override public Observable<PostRealm> deletePostIntent() {
    return deletePostSubject;
  }

  @NonNull @Override public Observable<CommentRealm> deleteCommentIntent() {
    return deleteCommentSubject;
  }

  @NonNull @Override public Observable<Pair<PostRealm, Integer>> votePostIntent() {
    return votePostSubject;
  }

  @NonNull @Override public Observable<Pair<CommentRealm, Integer>> voteCommentIntent() {
    return voteCommentSubject;
  }

  @NonNull @Override public Observable<CommentRealm> acceptCommentIntent() {
    return acceptCommentSubject;
  }

  @Override public void render(PostDetailViewState viewState) {
    Timber.d("render() %s", viewState);
    if (viewState.isDeletePost() && viewState.getDeletePostError() == null) {
      if (modelUpdateEvent != null) {
        modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, viewState.getData().get(0));
      }
      getRouter().handleBack();
      return;
    }

    if (viewState.isVotePost() && viewState.getVotePostError() == null) {
      if (modelUpdateEvent != null) {
        modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, viewState.getData().get(0));
      }
    }

    if (!viewState.isLoadingFirstPage()
        && !viewState.isLoadingPullToRefresh()
        && viewState.getFirstPageError() == null
        && viewState.getBookmarkError() == null
        && viewState.getNewCommentError() == null
        && viewState.getDeleteCommentError() == null
        && viewState.getVotePostError() == null
        && viewState.getVoteCommentError() == null) {
      renderShowData(viewState);
    } else if (viewState.isLoadingFirstPage()) {
      renderFirstPageLoading();
    } else if (viewState.getFirstPageError() != null) {
      renderFirstPageError();
    }
  }

  private void renderFirstPageError() {
    // TODO: 1.06.2017 change to stateview
  }

  private void renderFirstPageLoading() {

  }

  private void renderShowData(PostDetailViewState viewState) {
    epoxyController.setData(viewState);
    swipeRefreshLayout.setRefreshing(viewState.isLoadingPullToRefresh());
  }

  @NonNull @Override public PostDetailMviPresenter createPresenter() {
    return new PostDetailMviPresenter(CommentRepositoryProvider.getRepository(),
        PostRepositoryProvider.getRepository());
  }

  @Override public void setRestoringViewState(boolean restoringViewState) {
    this.restoringViewState = restoringViewState;
  }
}
