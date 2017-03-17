package com.yoloo.android.feature.feed.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatDrawableManager;
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

import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.ogaclejapan.arclayout.ArcLayout;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.news.NewsRepository;
import com.yoloo.android.data.repository.news.datasource.NewsRemoteDataStore;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.category.CategoryDetailController;
import com.yoloo.android.feature.chat.conversationlist.ConversationListController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.EditorType;
import com.yoloo.android.feature.editor.SendPostDelegate;
import com.yoloo.android.feature.editor.editorcategorylist.EditorCategoryListController;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.bountybutton.BountyButtonModel;
import com.yoloo.android.feature.feed.global.FeedGlobalController;
import com.yoloo.android.feature.login.AuthController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.news.NewsController;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.changehandler.ArcFadeMoveChangeHandler;
import com.yoloo.android.ui.changehandler.CircularRevealChangeHandlerCompat;
import com.yoloo.android.ui.changehandler.SharedElementDelayingChangeHandler;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.RevealFrameLayout;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.ui.widget.materialbadge.MenuItemBadge;
import com.yoloo.android.util.AnimUtils;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class FeedHomeController extends MvpController<FeedHomeView, FeedHomePresenter>
    implements FeedHomeView, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnReadMoreClickListener, OnShareClickListener,
    OnCommentClickListener, BountyButtonModel.OnBountyButtonClickListener,
    OnVoteClickListener, OnContentImageClickListener, OnModelUpdateEvent {

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.drawer_layout_feed) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view_feed) NavigationView navigationView;
  @BindView(R.id.toolbar_feed) Toolbar toolbar;
  @BindView(R.id.rv_feed) RecyclerView rvFeed;
  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.menu_layout) RevealFrameLayout menuLayout;
  @BindView(R.id.menu_arc_layout) ArcLayout arcLayout;
  @BindView(R.id.fab_show_menu) FloatingActionButton fab;
  //@BindView(R.id.dimming_view) View dimmingView;
  //@BindView(R.id.fab_menu_feed) FloatingActionsMenu fabMenu;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;
  @BindColor(R.color.grey_700) int grey300;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;
  @BindInt(android.R.integer.config_longAnimTime) int longAnimTime;

  private FeedHomeAdapter adapter;

  private WeakHandler handler;

  private String userId;

  private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      PostRealm post = intent.getParcelableExtra(SendPostDelegate.KEY_ADD_POST);
      rvFeed.smoothScrollToPosition(3);
      handler.postDelayed(() -> adapter.addPostToBeginning(post), 450);
    }
  };

  public FeedHomeController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static FeedHomeController create() {
    return new FeedHomeController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_home_feed, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupPullToRefresh();
    setupToolbar();
    setHasOptionsMenu(true);
    setupNavigation();
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);

    handler = new WeakHandler();
    LocalBroadcastManager.getInstance(view.getContext()).registerReceiver(newPostReceiver,
        new IntentFilter(SendPostDelegate.SEND_POST_EVENT));
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    LocalBroadcastManager.getInstance(view.getContext()).unregisterReceiver(newPostReceiver);
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_feed_user, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);

    MenuItem menuMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuMessage, new MenuItemBadge.Builder());
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_search:
        startTransaction(SearchController.create(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_message:
        /*if (hasNotification) {
          MenuItemBadge.getBadgeTextView(item).setBadgeCount(23, true);

          MenuItemBadge.update(getActivity(), item, new MenuItemBadge.Builder()
              .iconDrawable(AppCompatResources.getDrawable(getApplicationContext(),
                  R.drawable.ic_email_black_24dp))
              .textBackgroundColor(primaryBlueColor));

          hasNotification = false;
        } else {
          MenuItemBadge.getBadgeTextView(item).setBadgeCount(0, true);

          MenuItemBadge.update(getActivity(), item, new MenuItemBadge.Builder()
              .iconDrawable(AppCompatResources.getDrawable(getApplicationContext(),
                  R.drawable.ic_email_outline_24dp)));

          hasNotification = true;
        }*/
        startTransaction(ConversationListController.create(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_notification:
        startTransaction(new NotificationController(), new VerticalChangeHandler());
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onAccountLoaded(AccountRealm account) {
    userId = account.getId();
    setupDrawerInfo(account);
  }

  @Override public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override public void onLoaded(List<FeedItem> items) {
    adapter.addFeedItems(items);
  }

  @Override public void showContent() {
    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onError(Throwable e) {
    rootView.setState(StateLayout.VIEW_STATE_ERROR);
    swipeRefreshLayout.setRefreshing(false);
    Timber.e("onError: %s", e);
  }

  @Override public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onRefresh() {
    adapter.clearPostsSection();
    getPresenter().loadPosts(true, 20);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    drawerLayout.closeDrawer(GravityCompat.START);

    switch (itemId) {
      case R.id.action_nav_profile:
        handler.postDelayed(
            () -> startTransaction(ProfileController.create(userId), new VerticalChangeHandler()),
            400);
        return false;
      case R.id.action_nav_bookmarks:
        handler.postDelayed(() -> {
              FeedGlobalController controller = FeedGlobalController.ofBookmarked();
              controller.setModelUpdateEvent(this);
              startTransaction(controller, new VerticalChangeHandler());
            },
            400);
        return false;
      case R.id.action_nav_settings:
        AuthUI.getInstance().signOut(getActivity());
        handler.postDelayed(
            () -> getRouter().setRoot(RouterTransaction.with(AuthController.create())
                .pushChangeHandler(new FadeChangeHandler())), 400);
        return false;
      default:
        return false;
    }
  }

  @Override public boolean handleBack() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
      return true;
    }
    if (menuLayout.getVisibility() == View.VISIBLE) {
      int x = (fab.getLeft() + fab.getRight()) / 2;
      int y = (fab.getTop() + fab.getBottom()) / 2;
      float radiusOfFab = 1f * fab.getWidth() / 2f;
      float radiusFromFabToRoot = (float) Math.hypot(
          Math.max(x, rootView.getWidth() - x),
          Math.max(y, rootView.getHeight() - y));

      hideMenu(x, y, radiusFromFabToRoot, radiusOfFab);
      fab.setSelected(!fab.isSelected());
      return true;
    }
    return super.handleBack();
  }

  @NonNull @Override public FeedHomePresenter createPresenter() {
    return new FeedHomePresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()
        ),
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        NewsRepository.getInstance(
            NewsRemoteDataStore.getInstance()
        ));
  }

  @Override public void onCommentClick(View v, PostRealm post) {
    CommentController controller = CommentController.create(post);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onPostOptionsClick(View v, EpoxyModel<?> model, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    final boolean self = userId.equals(post.getOwnerId());
    menu.getMenu().getItem(2).setVisible(self);
    menu.getMenu().getItem(3).setVisible(self);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_feed_popup_bookmark:
          getPresenter().bookmarkPost(post.getId());
          Snackbar.make(getView(), R.string.label_feed_bookmarked, Snackbar.LENGTH_SHORT).show();
          return true;
        case R.id.action_feed_popup_edit:
          return true;
        case R.id.action_feed_popup_delete:
          getPresenter().deletePost(post.getId());
          adapter.delete(model);
          return true;
        default:
          return false;
      }
    });
  }

  @Override public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    startTransaction(ProfileController.create(userId),
        new TransitionChangeHandlerCompat(new ArcFadeMoveChangeHandler(), new FadeChangeHandler()));
  }

  @Override public void onReadMoreClick(View v, PostRealm post) {
    PostDetailController controller =
        PostDetailController.create(post.getId(), post.getAcceptedCommentId());
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new HorizontalChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onBountyButtonClick(View v) {
    FeedGlobalController controller = FeedGlobalController.ofBounty();
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override public void onContentImageClick(View v, String url) {
    startTransaction(PhotoController.create(url),
        new TransitionChangeHandlerCompat(new SharedElementDelayingChangeHandler(),
            new FadeChangeHandler()));
  }

  @OnClick(R.id.fab_show_menu) void openArcMenu(View v) {
    int x = (v.getLeft() + v.getRight()) / 2;
    int y = (v.getTop() + v.getBottom()) / 2;
    float radiusOfFab = 1f * v.getWidth() / 2f;
    float radiusFromFabToRoot = (float) Math.hypot(
        Math.max(x, rootView.getWidth() - x),
        Math.max(y, rootView.getHeight() - y));

    if (v.isSelected()) {
      hideMenu(x, y, radiusFromFabToRoot, radiusOfFab);
    } else {
      showMenu(x, y, radiusOfFab, radiusFromFabToRoot);
    }
    v.setSelected(!v.isSelected());
  }

  private void showMenu(int cx, int cy, float startRadius, float endRadius) {
    menuLayout.setVisibility(View.VISIBLE);

    Animator revealAnim =
        AnimUtils.createCircularReveal(menuLayout, cx, cy, startRadius, endRadius);
    revealAnim.setInterpolator(AnimUtils.getAccelerateDecelerateInterpolator());
    revealAnim.setDuration(200);
    revealAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        toolbar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        fab.setImageDrawable(AppCompatDrawableManager.get()
            .getDrawable(getActivity(), R.drawable.ic_clear_white_24dp));
        ViewUtils.setStatusBarColor(getActivity(), grey300);
      }
    });
    revealAnim.start();
  }

  private void hideMenu(int cx, int cy, float startRadius, float endRadius) {
    Animator revealAnim =
        AnimUtils.createCircularReveal(menuLayout, cx, cy, startRadius, endRadius);
    revealAnim.setInterpolator(AnimUtils.getAccelerateDecelerateInterpolator());
    revealAnim.setDuration(200);
    revealAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        toolbar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        menuLayout.setVisibility(View.GONE);
        fab.setImageDrawable(AppCompatDrawableManager.get()
            .getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
        ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
      }
    });
    revealAnim.start();
  }

  @OnClick(R.id.arc_menu_ask_question) void askQuestion() {
    Controller controller = EditorCategoryListController.create(EditorType.ASK_QUESTION);
    controller.addLifecycleListener(new LifecycleListener() {
      @Override public void onChangeEnd(@NonNull Controller controller,
          @NonNull ControllerChangeHandler changeHandler,
          @NonNull ControllerChangeType changeType) {
        menuLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        fab.setSelected(false);
        fab.setImageDrawable(AppCompatDrawableManager.get()
            .getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
        ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
      }
    });

    startTransaction(controller, new VerticalChangeHandler());
  }

  @OnClick(R.id.arc_menu_write_memory) void writeMemory() {
    Controller controller = EditorCategoryListController.create(EditorType.BLOG);
    controller.addLifecycleListener(new LifecycleListener() {
      @Override public void onChangeEnd(@NonNull Controller controller,
          @NonNull ControllerChangeHandler changeHandler,
          @NonNull ControllerChangeType changeType) {
        menuLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        fab.setSelected(false);
        fab.setImageDrawable(AppCompatDrawableManager.get()
            .getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
        ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
      }
    });

    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      adapter.updatePost(action, (PostRealm) payload);
    }
  }

  private void setupNavigation() {
    final ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);
  }

  private void setupDrawerInfo(AccountRealm account) {
    final View headerView = navigationView.getHeaderView(0);
    final ImageView ivNavAvatar = ButterKnife.findById(headerView, R.id.iv_nav_avatar);
    final TextView tvRealname = ButterKnife.findById(headerView, R.id.tv_nav_realname);
    final TextView tvUsername = ButterKnife.findById(headerView, R.id.tv_nav_username);

    //final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    if (account != null) {
      Glide.with(getActivity())
          .load(account.getAvatarUrl())
          .bitmapTransform(new CropCircleTransformation(getActivity()))
          .into(ivNavAvatar);

      tvRealname.setText(account.getRealname());
      tvUsername.setText(account.getUsername());
    }

    ivNavAvatar.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(userId), new VerticalChangeHandler()),
          400);
    });
    tvUsername.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(userId), new VerticalChangeHandler()),
          400);
    });
    tvRealname.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(userId), new VerticalChangeHandler()),
          400);
    });
  }

  private void setupRecyclerView() {
    adapter = FeedHomeAdapter.builder(getActivity())
        .onProfileClickListener(this)
        .onBountyButtonClickListener(this)
        .onCommentClickListener(this)
        .onContentImageClickListener(this)
        .onTrendingCategoryHeaderClickListener(
            v -> startTransaction(CategoryDetailController.create(), new VerticalChangeHandler()))
        .onTrendingCategoryItemClickListener((v, model, item) -> {
          FeedGlobalController controller = FeedGlobalController.ofCategory(item.getName());
          controller.setModelUpdateEvent(this);
          startTransaction(controller, new VerticalChangeHandler());
        })
        .onNewsHeaderClickListener(
            v -> startTransaction(NewsController.create(), new CircularRevealChangeHandlerCompat()))
        .onNewsItemClickListener((v, model, item) -> {

        })
        .onNewcomersItemClickListener((v, model, item) -> {
          Timber.d("Clicked.");
        })
        .onNewcomersFollowClickListener((v, model, account, direction) -> {
          Timber.d("Followed.");
          adapter.deleteNewcomersModel(model);
        })
        .onOptionsClickListener(this)
        .onReadMoreClickListener(this)
        .onVoteClickListener(this)
        .onShareClickListener(this)
        .build();

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(lm);
    rvFeed.addItemDecoration(new SpaceItemDecoration(12, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(adapter);

    rvFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener(5) {
      @Override public void onLoadMore() {
        /*handler.postDelayed(
            () -> getPresenter().loadPosts(true, UUID.randomUUID().toString(), eTag, 20), 700);*/
      }
    });

    rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
          fab.hide();
        } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
          fab.show();
        }
      }
    });
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(getResources().getString(R.string.label_feed_toolbar_title));
    }
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}
