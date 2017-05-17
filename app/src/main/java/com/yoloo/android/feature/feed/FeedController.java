package com.yoloo.android.feature.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.claudiodegio.msv.MaterialSearchView;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.blog.BlogController;
import com.yoloo.android.feature.bloglist.BlogListController;
import com.yoloo.android.feature.chat.NewChatListenerService;
import com.yoloo.android.feature.chat.chatlist.ChatListController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.editor.BlogEditorController;
import com.yoloo.android.feature.editor.editor.PostEditorController;
import com.yoloo.android.feature.editor.job.SendPostJob;
import com.yoloo.android.feature.explore.ExploreController;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.feature.groupgridoverview.GroupGridOverviewController;
import com.yoloo.android.feature.models.newusers.NewUserListModelGroup;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.recommendedgroups.RecommendedGroupListModelGroup;
import com.yoloo.android.feature.models.trendingblogs.TrendingBlogListModelGroup;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.notification.NotificationProvider;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.feature.settings.SettingsController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.ui.widget.floatingactionmenu.OptionsFabLayout;
import com.yoloo.android.ui.widget.materialbadgetextview.MenuItemBadge;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.NetworkUtil;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

import static com.yoloo.android.feature.base.BaseActivity.REQUEST_INVITE;

public class FeedController extends MvpController<FeedView, FeedPresenter>
    implements FeedView, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener, OnModelUpdateEvent, PostCallbacks,
    RecommendedGroupListModelGroup.Callbacks, TrendingBlogListModelGroup.Callbacks,
    NewUserListModelGroup.Callbacks {

  private static final String KEY_FEED_SHOWCASE_WELCOME = "SHOWCASE_FEED_WELCOME";
  private static final String KEY_FEED_SHOWCASE_FAB = "SHOWCASE_FEED_FAB";

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.rv_feed) RecyclerView rvFeed;
  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.fab_menu) OptionsFabLayout fab;
  @BindView(R.id.msv) MaterialSearchView msv;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;
  @BindColor(R.color.grey_700) int grey700;

  @BindDrawable(R.drawable.ic_email_outline) Drawable emptyMessageDrawable;
  @BindDrawable(R.drawable.ic_email_black_24dp) Drawable hasMessageDrawable;
  @BindDrawable(R.drawable.ic_notifications_none_black_24dp) Drawable emptyNotificationDrawable;

  @BindString(R.string.label_feed_toolbar_title) String feedToolbarTitleString;
  @BindString(R.string.app_name) String appNameString;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;
  @BindInt(android.R.integer.config_longAnimTime) int longAnimTime;

  private FeedEpoxyController epoxyController;

  private WeakHandler handler;

  private AccountRealm me;

  private MenuItem menuItemMessage;
  private MenuItem menuItemNotification;

  private TutoShowcase welcomeShowcase;
  private TutoShowcase fabShowcase;

  private boolean reEnter;

  private int newMessageCount = 0;

  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      PostRealm post = Parcels.unwrap(intent.getParcelableExtra(SendPostJob.KEY_ADD_POST));
      rvFeed.smoothScrollToPosition(3);
      handler.postDelayed(() -> epoxyController.addPost(post,
          epoxyController.getAdapter().getItemCount() > 3 ? 3 : 2), 400);
    }
  };

  private BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      MenuItemBadge.getBadgeTextView(menuItemNotification).setHighLightMode();
    }
  };

  private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      MenuItemBadge.update(getActivity(), menuItemMessage, new MenuItemBadge.Builder()
          .iconDrawable(hasMessageDrawable)
          .textBackgroundColor(Color.parseColor("#36b100"))
          .iconTintColor(Color.WHITE));
      newMessageCount++;
      MenuItemBadge.getBadgeTextView(menuItemMessage).setBadgeCount(newMessageCount);
    }
  };

  public FeedController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static FeedController create() {
    return new FeedController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_feed, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupPullToRefresh();
    setHasOptionsMenu(true);
    setupRecyclerView();

    handler = new WeakHandler();

    fab.setMainFabOnClickListener(v -> {
      if (!NetworkUtil.isNetworkAvailable(getActivity())) {
        Snackbar.make(getView(), R.string.error_network_unavailable, Snackbar.LENGTH_SHORT).show();
        return;
      }

      if (fab.isOptionsMenuOpened()) {
        fab.closeOptionsMenu();
      }
    });
    fab.setMiniFabSelectedListener(menuItem -> {
      final int itemId = menuItem.getItemId();

      switch (itemId) {
        case R.id.action_ask_question:
          fab.closeOptionsMenu();
          startTransaction(PostEditorController.create(), new VerticalChangeHandler());
          break;
        case R.id.action_write_blog:
          fab.closeOptionsMenu();
          startTransaction(BlogEditorController.create(), new VerticalChangeHandler());
          break;
      }
    });
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    if (!reEnter) {
      getPresenter().loadFeed(false);
      reEnter = true;
    }

    setupNavigation();
    showWelcomeTutorial();

    LocalBroadcastManager
        .getInstance(view.getContext())
        .registerReceiver(newPostReceiver, new IntentFilter(SendPostJob.SEND_POST_EVENT));

    LocalBroadcastManager
        .getInstance(view.getContext())
        .registerReceiver(newMessageReceiver,
            new IntentFilter(NewChatListenerService.NEW_MESSAGE_EVENT));

    LocalBroadcastManager
        .getInstance(view.getContext())
        .registerReceiver(newNotificationReceiver, new IntentFilter(NotificationProvider.TAG));

    rootView.setViewStateListener((stateView, viewState) -> {
      if (viewState == StateLayout.VIEW_STATE_ERROR) {
        View errorActionView = ButterKnife.findById(stateView, R.id.error_view);
        errorActionView.setOnClickListener(v -> getPresenter().loadFeed(false));
      }
    });
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    LocalBroadcastManager.getInstance(view.getContext()).unregisterReceiver(newPostReceiver);
    LocalBroadcastManager.getInstance(view.getContext())
        .unregisterReceiver(newNotificationReceiver);
    LocalBroadcastManager.getInstance(view.getContext()).unregisterReceiver(newMessageReceiver);
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_feed, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);

    menuItemMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuItemMessage, new MenuItemBadge.Builder()
        .iconDrawable(emptyMessageDrawable)
        .iconTintColor(Color.WHITE));

    menuItemNotification = menu.findItem(R.id.action_feed_notification);
    MenuItemBadge.update(getActivity(), menuItemNotification, new MenuItemBadge.Builder()
        .iconDrawable(emptyNotificationDrawable)
        .iconTintColor(Color.WHITE));
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_notification:
        MenuItemBadge.getBadgeTextView(menuItemNotification).clearHighLightMode();
        startTransaction(new NotificationController(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_search:
        startTransaction(SearchController.create(), new SimpleSwapChangeHandler());
        return true;
      case R.id.action_feed_message:
        newMessageCount = 0;
        MenuItemBadge.getBadgeTextView(menuItemMessage).setBadgeCount(newMessageCount, true);
        MenuItemBadge.update(getActivity(), menuItemMessage, new MenuItemBadge.Builder()
            .iconDrawable(emptyMessageDrawable)
            .iconTintColor(Color.WHITE));

        getRouter().pushController(RouterTransaction
            .with(ChatListController.create())
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler())
            .tag(ChatListController.class.getName()));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onMeLoaded(@NonNull AccountRealm me) {
    this.me = me;
    setupDrawerInfo();
    addUserPhotoToToolbar(me);

    epoxyController.setUserId(me.getId());
  }

  @Override public void onPostUpdated(@NonNull PostRealm post) {
    epoxyController.updatePost(post);
  }

  private void addUserPhotoToToolbar(AccountRealm me) {
    int toolbarAvatarSize = DisplayUtil.dpToPx(28);

    Glide
        .with(getActivity())
        .load(me.getAvatarUrl())
        .override(toolbarAvatarSize, toolbarAvatarSize)
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(new SimpleTarget<GlideDrawable>() {
          @Override
          public void onResourceReady(GlideDrawable resource,
              GlideAnimation<? super GlideDrawable> glideAnimation) {
            toolbar.setNavigationIcon(resource);
          }
        });
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override public void onLoaded(List<FeedItem<?>> value) {
    epoxyController.setData(value, false);
  }

  @Override public void onMoreLoaded(List<FeedItem<?>> items) {
    if (items.isEmpty()) {

      //epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(items);
    }
  }

  @Override
  public void showContent() {
    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onError(Throwable e) {
    rootView.setState(StateLayout.VIEW_STATE_ERROR);
    swipeRefreshLayout.setRefreshing(false);

    Timber.e("onError: %s", e);

    if (e.getMessage().contains("401")) {
      getRouter().setRoot(RouterTransaction.with(WelcomeController.create()));
    }
  }

  @Override
  public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onRefresh() {
    endlessRecyclerOnScrollListener.resetState();
    getPresenter().loadFeed(true);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_nav_profile:
        handler.postDelayed(
            () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
            400);
        break;
      case R.id.action_nav_explore:
        handler.postDelayed(
            () -> startTransaction(ExploreController.create(), new FadeChangeHandler()), 400);
        break;
      case R.id.action_nav_bookmarks:
        handler.postDelayed(() -> {
          PostListController controller = PostListController.ofBookmarked();
          controller.setModelUpdateEvent(this);
          startTransaction(controller, new FadeChangeHandler());
        }, 400);
        break;
      case R.id.action_nav_invite_friends:
        handler.postDelayed(this::onInviteClicked, 400);
        break;
      case R.id.action_nav_settings:
        handler.postDelayed(
            () -> startTransaction(SettingsController.create(), new FadeChangeHandler()), 400);
        break;
      case R.id.action_nav_feedback:
        handler.postDelayed(() -> {
          Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
          emailIntent.setData(Uri.parse("mailto: hello@yolooapp.com"));
          startActivity(Intent.createChooser(emailIntent, "Send feedback"));
        }, 400);
        break;
      default:
        break;
    }
    // Close the navigation drawer when an item is selected.
    item.setChecked(false);
    getDrawerLayout().closeDrawers();
    return true;
  }

  @Override
  public boolean handleBack() {
    if (getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
      getDrawerLayout().closeDrawer(GravityCompat.START);
      return true;
    }

    if (fab.isOptionsMenuOpened()) {
      fab.closeOptionsMenu();
      return true;
    }

    return super.handleBack();
  }

  @NonNull
  @Override
  public FeedPresenter createPresenter() {
    return new FeedPresenter(PostRepositoryProvider.getRepository(),
        GroupRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  @Override
  public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      if (action == FeedAction.UPDATE) {
        epoxyController.updatePost((PostRealm) payload);
      } else if (action == FeedAction.DELETE) {
        epoxyController.deletePost((PostRealm) payload);
      }
    }
  }

  private void setupNavigation() {
    DrawerLayout drawerLayout = getDrawerLayout();
    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
    final ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    getNavigationView().setNavigationItemSelectedListener(this);
  }

  private void setupDrawerInfo() {
    NavigationView navigationView = getNavigationView();
    DrawerLayout drawerLayout = getDrawerLayout();

    final View headerView = navigationView.getHeaderView(0);
    final ImageView ivNavAvatar = ButterKnife.findById(headerView, R.id.iv_nav_avatar);
    final TextView tvRealname = ButterKnife.findById(headerView, R.id.tv_nav_realname);
    final TextView tvUsername = ButterKnife.findById(headerView, R.id.tv_nav_username);

    if (me != null) {
      Glide
          .with(getActivity())
          .load(me.getAvatarUrl())
          .bitmapTransform(new CropCircleTransformation(getActivity()))
          .into(ivNavAvatar);

      tvRealname.setText(me.getRealname());
      tvUsername.setText(me.getUsername());
    }

    ivNavAvatar.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
    tvUsername.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
    tvRealname.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
          400);
    });
  }

  private void setupRecyclerView() {
    epoxyController = new FeedEpoxyController(getActivity());
    epoxyController.setPostCallbacks(this);
    epoxyController.setRecommendedGroupListCallbacks(this);
    epoxyController.setTrendingBlogListCallbacks(this);
    epoxyController.setOnBountyButtonClickListener(v -> {
      PostListController controller = PostListController.ofBounty();
      controller.setModelUpdateEvent(this);
      startTransaction(controller, new VerticalChangeHandler());
    });
    epoxyController.setNewUserListModelGroupCallbacks(this);
    epoxyController.setOnNewUserWelcomeClickListener(v -> Timber.d("New User item"));

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(lm);
    rvFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(epoxyController.getAdapter());

    endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(lm) {
      @Override
      public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
        if (swipeRefreshLayout.isRefreshing()) {
          endlessRecyclerOnScrollListener.resetState();
          return;
        }

        getPresenter().loadMorePosts();
        epoxyController.showLoader();
      }
    };

    rvFeed.addOnScrollListener(endlessRecyclerOnScrollListener);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(primaryColor);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    ab.setTitle(feedToolbarTitleString);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void onInviteClicked() {
    Resources res = getResources();

    Intent intent = new AppInviteInvitation.IntentBuilder(res.getString(R.string.invitation_title))
        .setMessage(res.getString(R.string.invitation_message, me.getUsername()))
        .setCustomImage(Uri.parse(res.getString(R.string.invitation_custom_image)))
        .setDeepLink(Uri.parse(res.getString(R.string.invitation_deep_link)))
        .setCallToActionText(res.getString(R.string.invitation_cta))
        .build();
    startActivityForResult(intent, REQUEST_INVITE);
  }

  private void showWelcomeTutorial() {
    welcomeShowcase = TutoShowcase
        .from(getActivity())
        .setContentView(R.layout.showcase_feed_welcome)
        .setFitsSystemWindows(true)
        .on(toolbar)
        .onClickContentView(R.id.tv_showcase_next, v -> {
          welcomeShowcase.dismiss();
          v.postDelayed(this::showFabTutorial, 500);
        })
        .showOnce(KEY_FEED_SHOWCASE_WELCOME);
  }

  private void showFabTutorial() {
    fabShowcase = TutoShowcase
        .from(getActivity())
        .setContentView(R.layout.showcase_feed_fab)
        .setFitsSystemWindows(true)
        .on(R.id.hacky_view)
        .addCircle()
        .withBorder()
        .onClick(v -> fabShowcase.dismiss())
        .onClickContentView(R.id.tv_showcase_got_it, v -> fabShowcase.dismiss())
        .showOnce(KEY_FEED_SHOWCASE_FAB);
  }

  @Override public void onPostClickListener(@NonNull PostRealm post) {
    if (post.isBlogPost()) {
      BlogController controller = BlogController.create(post);
      controller.setModelUpdateEvent(this);
      startTransaction(controller, new HorizontalChangeHandler());
    } else {
      PostDetailController controller = PostDetailController.create(post.getId());
      controller.setModelUpdateEvent(this);
      startTransaction(controller, new HorizontalChangeHandler());
    }
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
          getPresenter().deletePost(post.getId());
          epoxyController.deletePost(post);
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
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onPostCommentClickListener(@NonNull PostRealm post) {
    CommentController controller =
        CommentController.create(post.getId(), post.getOwnerId(), post.getPostType(),
            post.getAcceptedCommentId() != null);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onPostVoteClickListener(@NonNull PostRealm post, int direction) {
    getPresenter().votePost(post.getId(), direction);
  }

  @Override public void onPostTagClickListener(@NonNull String tagName) {
    Timber.d("Tag name: %s", tagName);
  }

  @Override public void onRecommendedGroupsHeaderClicked() {
    GroupGridOverviewController controller =
        GroupGridOverviewController.create(2, true, false);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onRecommendedGroupClicked(GroupRealm group) {
    GroupController controller = GroupController.create(group.getId());
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override public void onTrendingBlogHeaderClicked() {
    startTransaction(BlogListController.create(), new VerticalChangeHandler());
  }

  @Override public void onTrendingBlogClicked(@NonNull PostRealm blog) {
    startTransaction(BlogController.create(blog), new HorizontalChangeHandler());
  }

  @Override public void onTrendingBlogBookmarkClicked(@NonNull PostRealm post) {
    if (post.isBookmarked()) {
      getPresenter().unBookmarkPost(post.getId());
    } else {
      getPresenter().bookmarkPost(post.getId());
    }
  }

  @Override public void onTrendingBlogOptionsClicked(View v, @NonNull PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        getPresenter().deletePost(post.getId());
        epoxyController.deletePost(post);
        return true;
      }

      return false;
    });
  }

  @Override public void onNewUserListHeaderClicked() {

  }

  @Override public void onNewUserClicked(AccountRealm account) {
    startTransaction(ProfileController.create(account.getId()),
        new VerticalChangeHandler());
  }

  @Override public void onNewUserFollowClicked(AccountRealm account, int direction) {
    getPresenter().follow(account.getId(), direction);
    epoxyController.deleteNewUser(account);
  }
}
