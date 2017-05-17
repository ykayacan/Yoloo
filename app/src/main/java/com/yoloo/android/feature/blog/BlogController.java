package com.yoloo.android.feature.blog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;
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
import com.yoloo.android.ui.widget.SliderView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.NetworkUtil;
import com.yoloo.android.util.ShareUtil;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

public class BlogController extends MvpController<BlogView, BlogPresenter>
    implements BlogView, CommentInput.NewCommentListener, CommentCallbacks, PostCallbacks {

  private static final String KEY_POST = "POST_ID";

  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.slider) SliderView sliderView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.layout_input) CommentInput input;

  private OnModelUpdateEvent modelUpdateEvent;

  private BlogEpoxyController epoxyController;

  private PostRealm post;

  private boolean reEnter;

  private KeyboardUtil.SoftKeyboardToggleListener keyboardToggleListener;

  public BlogController(Bundle args) {
    super(args);
  }

  public static BlogController create(@NonNull PostRealm post) {
    Bundle bundle = new BundleBuilder().putParcelable(KEY_POST, Parcels.wrap(post)).build();

    return new BlogController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_blog, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupToolbar();
    setupRecyclerView();

    post = Parcels.unwrap(getArgs().getParcelable(KEY_POST));
    sliderView.setImageUrls(Stream.of(post.getMedias()).map(MediaRealm::getMediumSizeUrl).toList());

    input.setPostId(post.getId());
    input.setNewCommentListener(this);

    epoxyController.addBlog(post);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (!reEnter) {
      getPresenter().loadComments(post);
      reEnter = true;
    }

    keyboardToggleListener = isVisible -> {
      if (isVisible) {
        Timber.d("Count: %s", recyclerView.getAdapter().getItemCount());
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
  public void onNewComment(CommentRealm comment) {
    epoxyController.addComment(comment);
    epoxyController.scrollToEnd(recyclerView);
  }

  @Override public void onMeLoaded(AccountRealm me) {
    epoxyController.setUserId(me.getId());
  }

  @Override public void onPostUpdated(PostRealm post) {
    epoxyController.updateBlog(post);

    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.UPDATE, post);
    }
  }

  @Override public void onCommentUpdated(CommentRealm comment) {
    epoxyController.updateComment(comment);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<CommentRealm> value) {
    if (value.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.addComments(value);
    }
  }

  @Override
  public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override
  public void onEmpty() {

  }

  @NonNull
  @Override
  public BlogPresenter createPresenter() {
    return new BlogPresenter(PostRepositoryProvider.getRepository(),
        CommentRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerView() {
    epoxyController = new BlogEpoxyController(getActivity(), Glide.with(getActivity()));
    epoxyController.setPostCallbacks(this);
    epoxyController.setCommentCallbacks(this);

    LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setLayoutManager(lm);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(epoxyController.getAdapter());

    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(lm) {
          @Override public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            getPresenter().loadMoreComments();
            epoxyController.showLoader();
          }
        };

    recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  public void setModelUpdateEvent(OnModelUpdateEvent modelUpdateEvent) {
    this.modelUpdateEvent = modelUpdateEvent;
  }

  private void deletePost(PostRealm post) {
    getPresenter().deletePost(post.getId());
    if (modelUpdateEvent != null) {
      modelUpdateEvent.onModelUpdateEvent(FeedAction.DELETE, post);
    }
    getRouter().handleBack();
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
    ShareUtil.share(this, post.getTitle(), post.getContent());
  }

  @Override public void onPostCommentClickListener(@NonNull PostRealm post) {
    input.showKeyboard();
    epoxyController.scrollToEnd(recyclerView);
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
    KeyboardUtil.hideKeyboard(getView());
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onCommentMentionClickListener(@NonNull String username) {
    Snackbar.make(getView(), username, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onCommentVoteClickListener(@NonNull CommentRealm comment, int direction) {
    getPresenter().voteComment(comment.getId(), direction);
  }

  @Override public void onCommentAcceptRequestClickListener(@NonNull CommentRealm comment) {
    // empty
  }
}
