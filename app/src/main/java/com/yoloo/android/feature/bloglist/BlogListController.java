package com.yoloo.android.feature.bloglist;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import java.util.List;
import timber.log.Timber;

public class BlogListController extends MvpController<BlogListView, BlogListPresenter>
    implements BlogListView {

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

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      stateLayout.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<PostRealm> value) {
    epoxyController.setData(value, false);

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

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerView() {
    epoxyController = new BlogListEpoxyController(getActivity(), Glide.with(getActivity()));

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvBlogList.setLayoutManager(lm);
    rvBlogList.addItemDecoration(new SpaceItemDecoration(12, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvBlogList.setItemAnimator(animator);

    rvBlogList.setHasFixedSize(true);
    rvBlogList.setAdapter(epoxyController.getAdapter());
  }
}
