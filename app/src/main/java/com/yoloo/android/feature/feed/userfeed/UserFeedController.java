package com.yoloo.android.feature.feed.userfeed;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.faker.CommentFaker;
import com.yoloo.android.data.faker.PostFaker;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.category.MainCatalogController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.globalfeed.GlobalFeedController;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import com.yoloo.android.feature.ui.widget.badgeview.MenuItemBadge;
import com.yoloo.android.feature.ui.widget.floatingactionmenu.widget.FloatingActionMenu;
import com.yoloo.android.feature.write.catalog.CatalogController;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.CropCircleTransformation;
import java.util.List;
import timber.log.Timber;

public class UserFeedController extends MvpController<UserFeedView, UserFeedPresenter>
    implements UserFeedView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener, OnOptionsClickListener,
    OnReadMoreClickListener, OnShareClickListener, OnCommentClickListener,
    FeedAdapter.OnBountyClickListener, FeedAdapter.OnCategoryClickListener,
    FeedAdapter.OnExploreCategoriesClickListener, OnVoteClickListener, OnContentImageClickListener,
    OnModelUpdateListener {

  @BindView(R.id.drawer_layout_feed) DrawerLayout drawerLayout;

  @BindView(R.id.nav_view_feed) NavigationView navigationView;

  @BindView(R.id.toolbar_feed) Toolbar toolbar;

  @BindView(R.id.rv_feed) RecyclerView rvFeed;

  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;

  @BindView(R.id.view_feed_action) View actionView;

  @BindView(R.id.dimming_view) View dimmingView;

  @BindView(R.id.fab_menu_feed) FloatingActionMenu fabMenu;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private FeedAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private String cursor;
  private String eTag;

  private boolean reEnter;

  public UserFeedController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed_user, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupNavigation();
    setupRecyclerView();

    fabMenu.setupWithDimmingView(dimmingView,
        ContextCompat.getColor(getActivity(), R.color.fab_dim));
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    if (VersionUtil.hasL()) {
      getActivity().getWindow().setStatusBarColor(primaryDarkColor);
    }

    if (!reEnter) {
      getPresenter().loadTrendingCategories();
      getPresenter().loadFeed(false, cursor, eTag, 20);

      reEnter = true;
    }
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_feed_user, menu);

    DrawableHelper.withContext(getActivity()).withColor(android.R.color.white).applyTo(menu);

    MenuItem menuMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuMessage, new MenuItemBadge.Builder().iconDrawable(
        AppCompatResources.getDrawable(getActivity(), R.drawable.ic_email_black_24dp))
        .textBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary_blue))
        .textColor(Color.WHITE));
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_notification:
        CommentFaker.generate();
        AccountFaker.generate();
        new WeakHandler().postDelayed(() -> getPresenter().loadTrendingCategories(), 250);
        return true;
      case R.id.action_feed_search:
        PostFaker.generate();
        new WeakHandler().postDelayed(() -> getPresenter().loadFeed(false, null, null, 20), 250);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onPostingSuccessful(PostRealm item) {

  }

  @Override public void onPostingFailed(Throwable t) {

  }

  @Override public void onTrendingCategoriesLoaded(List<CategoryRealm> topics) {
    adapter.updateTrendingCategories(topics);
  }

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();
    adapter.addPosts(value.getData());
  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {
    actionView.setVisibility(View.VISIBLE);
  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();

    getPresenter().loadFeed(true, cursor, eTag, 20);
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @NonNull @Override public UserFeedPresenter createPresenter() {
    return new UserFeedPresenter(PostRepository.getInstance(PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance()),
        CategoryRepository.getInstance(CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    final int id = item.getItemId();

    switch (id) {

    }

    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override public boolean handleBack() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
      return true;
    }
    return super.handleBack();
  }

  @Override public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    getRouter().pushController(RouterTransaction.with(
        CommentController.create(itemId, acceptedCommentId, (long) v.getTag()))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
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
          return true;
      }
      return false;
    });
  }

  @Override public void onProfileClick(View v, String ownerId) {
    Toast.makeText(getActivity(), "Profile clicked! " + ownerId, Toast.LENGTH_SHORT).show();
  }

  @Override public void onReadMoreClickListener(View v, String postId, String acceptedCommentId,
      long modelId) {
    getRouter().pushController(RouterTransaction.with(
        PostDetailController.create(postId, acceptedCommentId, modelId, this))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onShareClick(View v) {
    Toast.makeText(getActivity(), "Share clicked!", Toast.LENGTH_SHORT).show();
  }

  @Override public void onBountyClick(View v) {
    Snackbar.make(getView(), "Bounty clicked!", Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onCategoryClick(View v, String categoryId, String name) {
    getRouter().pushController(RouterTransaction.with(GlobalFeedController.create(name))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onExploreCategoriesClick(View v) {
    getRouter().pushController(RouterTransaction.with(new MainCatalogController())
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onVoteClick(String votableId, int direction, @VotableType int type) {
    getPresenter().vote(votableId, direction);
  }

  @Override public void onContentUpdate(EpoxyModel<?> model) {
    //adapter.update(model);
  }

  @Override public void onContentImageClick(View v, String url) {
    PhotoController toController = PhotoController.create(url);

    getRouter().pushController(RouterTransaction.with(toController)
        .pushChangeHandler(new ImageChangeHandler())
        .popChangeHandler(new ImageChangeHandler()));
  }

  @Override public void onModelUpdate(long modelId, Object payload) {
    adapter.update(modelId, payload);
  }

  @OnClick(R.id.fab_ask_question) void openWriteScreen() {
    fabMenu.collapseImmediately();
    getRouter().pushController(RouterTransaction.with(new CatalogController())
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupNavigation() {
    final ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    final View headerView = navigationView.getHeaderView(0);
    final ImageView ivNavAvatar = ButterKnife.findById(headerView, R.id.iv_nav_avatar);
    final TextView tvRealname = ButterKnife.findById(headerView, R.id.tv_nav_realname);
    final TextView tvUsername = ButterKnife.findById(headerView, R.id.tv_nav_username);

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    if (user.getPhotoUrl() != null) {
      Glide.with(getActivity())
          .load(user.getPhotoUrl())
          .bitmapTransform(CropCircleTransformation.getInstance(getActivity()))
          .into(ivNavAvatar);
    }

    tvRealname.setText(user.getDisplayName());
    tvUsername.setText(user.getDisplayName().trim().replace(" ", "").toLowerCase());
  }

  private void setupRecyclerView() {
    adapter = FeedAdapter.builder()
        .isMainFeed(true)
        .onProfileClickListener(this)
        .onBountyClickListener(this)
        .onCommentClickListener(this)
        .onContentImageClickListener(this)
        .onExploreCategoriesClickListener(this)
        .onOptionsClickListener(this)
        .onReadMoreClickListener(this)
        .onVoteClickListener(this)
        .onShareClickListener(this)
        .onCategoryClickListener(this)
        .build();

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(layoutManager);
    rvFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

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
    swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.primary));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(getResources().getString(R.string.label_feed_toolbar_title));
  }
}