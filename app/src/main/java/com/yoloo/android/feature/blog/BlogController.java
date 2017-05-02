package com.yoloo.android.feature.blog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.writecommentbox.CommentAutocomplete;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.widget.CommentView;
import com.yoloo.android.ui.widget.SliderView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

public class BlogController extends MvpController<BlogView, BlogPresenter>
    implements BlogView, CommentView.OnCommentClickListener,
    CommentAutocomplete.NewCommentListener {

  private static final String KEY_POST = "POST_ID";

  @BindView(R.id.recycler_view) RecyclerView rvBlog;
  @BindView(R.id.slider) SliderView sliderView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.layout_compose) CommentAutocomplete composeLayout;

  private BlogEpoxyController epoxyController;
  private PostRealm post;

  public BlogController() {
  }

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
    setupRecyclerview();

    post = Parcels.unwrap(getArgs().getParcelable(KEY_POST));
    sliderView.setImageUrls(Stream.of(post.getMedias()).map(MediaRealm::getMediumSizeUrl).toList());

    composeLayout.setPostId(post.getId());
    composeLayout.setNewCommentListener(this);

    epoxyController.addBlog(post);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    getPresenter().loadComments(post.getId());
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @Override
  public void onCommentLongClick(CommentRealm comment) {
    new AlertDialog.Builder(getActivity())
        .setItems(R.array.action_comment_dialog, (dialog, which) -> {
          if (which == 0) {
            getPresenter().deleteComment(comment);
            epoxyController.removeComment(comment);
          }
        })
        .show();
  }

  @Override
  public void onCommentProfileClick(String userId) {
    KeyboardUtil.hideKeyboard(getView());
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override
  public void onCommentVoteClick(String commentId, int direction) {
    getPresenter().voteComment(commentId, direction);
  }

  @Override
  public void onCommentMentionClick(String username) {
    Snackbar.make(getView(), username, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onMarkAsAccepted(CommentRealm comment) {
    getPresenter().acceptComment(comment);
  }

  @Override
  public void onNewComment(CommentRealm comment) {
    epoxyController.addComment(comment);
    rvBlog.smoothScrollToPosition(epoxyController.getAdapter().getItemCount() - 1);
  }

  @Override
  public void onCommentAccepted(CommentRealm comment) {
    Snackbar.make(getView(), R.string.label_comment_accepted_confirm, Snackbar.LENGTH_SHORT).show();
    post.setAcceptedCommentId(comment.getId());
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<CommentRealm> value) {
    epoxyController.addComments(value);
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
        CommentRepositoryProvider.getRepository());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerview() {
    epoxyController = new BlogEpoxyController(getActivity(), Glide.with(getActivity()));
    epoxyController.setOnCommentClickListener(this);

    rvBlog.setItemAnimator(new DefaultItemAnimator());
    rvBlog.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvBlog.setHasFixedSize(true);
    rvBlog.setAdapter(epoxyController.getAdapter());
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}
