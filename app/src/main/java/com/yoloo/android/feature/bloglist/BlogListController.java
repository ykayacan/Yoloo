package com.yoloo.android.feature.bloglist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import java.util.List;
import timber.log.Timber;

public class BlogListController extends MvpController<BlogListView, BlogListPresenter>
    implements BlogListView, OnBookmarkClickListener, OnCommentClickListener,
    OnItemClickListener<PostRealm>, OnPostOptionsClickListener, OnProfileClickListener,
    OnShareClickListener, OnVoteClickListener, OnModelUpdateEvent {

  @BindView(R.id.state_layout_bloglist) StateLayout stateLayout;
  @BindView(R.id.rv_bloglist) RecyclerView rvBlogList;
  @BindView(R.id.ctl_bloglist) CollapsingToolbarLayout collapsingToolbarLayout;
  @BindView(R.id.toolbar_bloglist) Toolbar toolbar;
  @BindView(R.id.iv_bloglist_cover) ImageView ivCover;

  private BlogListEpoxyController epoxyController;

  public static BlogListController create() {
    return new BlogListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_blog_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupRecyclerView();
    setupToolbar();

    Glide.with(getActivity()).load(R.drawable.blog_header_small).into(ivCover);
  }

  @NonNull
  @Override
  public BlogListPresenter createPresenter() {
    return new BlogListPresenter(PostRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    epoxyController.setUserId(me.getId());
  }

  @Override public void onPostUpdated(PostRealm post) {

  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      stateLayout.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<PostRealm> value) {
    if (value.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setData(value, false);
    }

    stateLayout.setState(StateLayout.VIEW_STATE_CONTENT);
  }

  @Override
  public void onError(Throwable e) {
    stateLayout.setState(StateLayout.VIEW_STATE_ERROR);

    Timber.e(e);
  }

  @Override
  public void onEmpty() {
    stateLayout.setState(StateLayout.VIEW_STATE_EMPTY);
  }

  @Override public void onBookmarkClick(@NonNull PostRealm post, boolean bookmark) {
    if (bookmark) {
      getPresenter().bookmarkPost(post.getId());
    } else {
      getPresenter().unBookmarkPost(post.getId());
    }
  }

  @Override public void onCommentClick(View v, PostRealm post) {
    CommentController controller =
        CommentController.create(post.getId(), post.getOwnerId(), post.getPostType(),
            post.getAcceptedCommentId() != null);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onItemClick(View v, PostRealm item) {
    PostDetailController controller = PostDetailController.create(item.getId());
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new HorizontalChangeHandler());
  }

  @Override public void onPostOptionsClick(View v, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        getPresenter().deletePost(post.getId());
        epoxyController.deletePost(post);
        return true;
      }

      return super.onOptionsItemSelected(item);
    });
  }

  @Override public void onProfileClick(View v, String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onPostVoteClick(PostRealm post, int direction) {
    getPresenter().votePost(post.getId(), direction);
    post.setVoteDir(direction);
    epoxyController.updatePost(post);
  }

  @Override public void onModelUpdateEvent(int action, @Nullable Object payload) {

  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerView() {
    epoxyController = new BlogListEpoxyController(getActivity(), Glide.with(getActivity()));
    epoxyController.setOnBookmarkClickListener(this);
    epoxyController.setOnCommentClickListener(this);
    epoxyController.setOnPostClickListener(this);
    epoxyController.setOnPostOptionsClickListener(this);
    epoxyController.setOnProfileClickListener(this);
    epoxyController.setOnShareClickListener(this);
    epoxyController.setOnVoteClickListener(this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvBlogList.setLayoutManager(lm);
    rvBlogList.addItemDecoration(new SpaceItemDecoration(12, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvBlogList.setItemAnimator(animator);

    rvBlogList.setHasFixedSize(true);
    rvBlogList.setAdapter(epoxyController.getAdapter());

    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(lm) {
          @Override public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            getPresenter().loadTrendingBlogs();
            epoxyController.showLoader();
          }
        };

    rvBlogList.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}
