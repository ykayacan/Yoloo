package com.yoloo.android.feature.feed.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ogaclejapan.arclayout.ArcLayout;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.util.GoogleApiHelper;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.blog.BlogListController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.EditorType;
import com.yoloo.android.feature.editor.editor.EditorController;
import com.yoloo.android.feature.editor.editor.EditorController2;
import com.yoloo.android.feature.editor.job.SendPostJob;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnModelUpdateEvent;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.feature.grouplist.GroupListController;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.changehandler.ArcFadeMoveChangeHandler;
import com.yoloo.android.ui.changehandler.SharedElementDelayingChangeHandler;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.RevealFrameLayout;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.AnimUtils;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

public class FeedHomeController extends MvpController<FeedHomeView, FeedHomePresenter>
    implements FeedHomeView, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnShareClickListener, OnCommentClickListener, OnVoteClickListener,
    OnContentImageClickListener, OnModelUpdateEvent, OnBookmarkClickListener,
    OnItemClickListener<PostRealm>, OnSearchViewListener,
    GoogleApiClient.OnConnectionFailedListener {

  private static final int REQUEST_INVITE = 0;

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.rv_feed) RecyclerView rvFeed;
  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.menu_layout) RevealFrameLayout menuLayout;
  @BindView(R.id.menu_arc_layout) ArcLayout arcLayout;
  @BindView(R.id.fab_show_menu) FloatingActionButton fab;
  @BindView(R.id.msv) MaterialSearchView msv;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;
  @BindColor(R.color.grey_700) int grey700;

  @BindString(R.string.label_feed_toolbar_title) String feedToolbarTitleString;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;
  @BindInt(android.R.integer.config_longAnimTime) int longAnimTime;

  private GoogleApiClient googleApiClient;

  private FeedHomeAdapter adapter;

  private WeakHandler handler;

  private AccountRealm me;

  private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      PostRealm post = Parcels.unwrap(intent.getParcelableExtra(SendPostJob.KEY_ADD_POST));
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

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupPullToRefresh();
    setHasOptionsMenu(true);
    setupRecyclerView();

    msv.setOnSearchViewListener(this);

    setupGoogleApi();

    // Check for App Invite invitations and launch deep-link activity if possible.
    // Requires that an Activity is registered in AndroidManifest.xml to handle
    // deep-link URLs.
    AppInvite.AppInviteApi
        .getInvitation(googleApiClient, getActivity(), true)
        .setResultCallback(result -> {
          Timber.d("getInvitation:onResult: %s", result.getStatus());
          if (result.getStatus().isSuccess()) {
            // Extract information from the intent
            Intent intent = result.getInvitationIntent();
            String deepLink = AppInviteReferral.getDeepLink(intent);
            String invitationId = AppInviteReferral.getInvitationId(intent);

            // Because autoLaunchDeepLink = true we don't have to do anything
            // here, but we could set that to false and manually choose
            // an Activity to launch to handle the deep link here.
          }
        });
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    setupNavigation();

    handler = new WeakHandler();
    LocalBroadcastManager
        .getInstance(view.getContext())
        .registerReceiver(newPostReceiver, new IntentFilter(SendPostJob.SEND_POST_EVENT));
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    LocalBroadcastManager.getInstance(view.getContext()).unregisterReceiver(newPostReceiver);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_feed_home, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);

    MenuItem item = menu.findItem(R.id.action_feed_search);
    msv.setMenuItem(item);
    //MenuItemBadge.update(getActivity(), item, new MenuItemBadge.Builder());
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_notification:
        startTransaction(new NotificationController(), new VerticalChangeHandler());
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Timber.d("onActivityResult: resultCode=%s, requestCode=%s", resultCode, requestCode);

    if (requestCode == REQUEST_INVITE) {
      if (resultCode == getActivity().RESULT_OK) {
        // Get the invitation IDs of all sent messages
        String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
        for (String id : ids) {
          Timber.d("onActivityResult: sent invitation: %s", id);
        }
      } else {
        // Sending failed or it was canceled, show failure message to the user
        Snackbar.make(getView(), R.string.send_failed, Snackbar.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    this.me = me;
    setupDrawerInfo();

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

    adapter.setUserId(me.getId());
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      rootView.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<FeedItem> items) {
    adapter.addFeedItems(items);
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
  }

  @Override
  public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onRefresh() {
    adapter.clearPostsSection();
    getPresenter().loadPosts(true, 20);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_nav_profile:
        handler.postDelayed(
            () -> startTransaction(ProfileController.create(me.getId()), new FadeChangeHandler()),
            400);
        break;
      case R.id.action_nav_bookmarks:
        handler.postDelayed(() -> {
          PostListController controller = PostListController.ofBookmarked();
          controller.setModelUpdateEvent(this);
          startTransaction(controller, new FadeChangeHandler());
        }, 400);
        break;
      case R.id.action_nav_invite_friends:
        onInviteClicked();
        break;
      case R.id.action_nav_settings:
        AuthUI.getInstance().signOut((FragmentActivity) getActivity());
        startTransaction(WelcomeController.create(), new FadeChangeHandler());
        break;
      default:
        break;
    }
    // Close the navigation drawer when an item is selected.
    item.setChecked(true);
    getDrawerLayout().closeDrawers();
    return true;
  }

  @Override
  public boolean handleBack() {
    if (getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
      getDrawerLayout().closeDrawer(GravityCompat.START);
      return true;
    }

    if (menuLayout.getVisibility() == View.VISIBLE) {
      int x = (fab.getLeft() + fab.getRight()) / 2;
      int y = (fab.getTop() + fab.getBottom()) / 2;
      float radiusOfFab = 1f * fab.getWidth() / 2f;
      float radiusFromFabToRoot = (float) Math.hypot(Math.max(x, rootView.getWidth() - x),
          Math.max(y, rootView.getHeight() - y));

      hideMenu(x, y, radiusFromFabToRoot, radiusOfFab);
      fab.setSelected(!fab.isSelected());
      return true;
    }
    return super.handleBack();
  }

  @NonNull
  @Override
  public FeedHomePresenter createPresenter() {
    return new FeedHomePresenter(PostRepositoryProvider.getRepository(),
        GroupRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  @Override
  public void onCommentClick(View v, PostRealm post) {
    CommentController controller = CommentController.create(post);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override
  public void onPostOptionsClick(View v, EpoxyModel<?> model, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    final boolean self = me.getId().equals(post.getOwnerId());
    menu.getMenu().getItem(1).setVisible(self);
    menu.getMenu().getItem(2).setVisible(self);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
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

  @Override
  public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    startTransaction(ProfileController.create(userId),
        new TransitionChangeHandlerCompat(new ArcFadeMoveChangeHandler(), new FadeChangeHandler()));
  }

  @Override
  public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override
  public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override
  public void onContentImageClick(View v, MediaRealm media) {
    startTransaction(FullscreenPhotoController.create(media.getLargeSizeUrl()),
        new TransitionChangeHandlerCompat(new SharedElementDelayingChangeHandler(),
            new FadeChangeHandler()));
  }

  @Override
  public void onBookmarkClick(@NonNull String postId, boolean bookmark) {
    if (bookmark) {
      getPresenter().bookmarkPost(postId);
    } else {
      getPresenter().unBookmarkPost(postId);
    }
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, PostRealm item) {
    PostDetailController controller =
        PostDetailController.create(item.getId(), item.getAcceptedCommentId());
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new HorizontalChangeHandler());
  }

  @OnClick(R.id.fab_show_menu)
  void openArcMenu(View v) {
    int x = (v.getLeft() + v.getRight()) / 2;
    int y = (v.getTop() + v.getBottom()) / 2;
    float radiusOfFab = 1f * v.getWidth() / 2f;
    float radiusFromFabToRoot = (float) Math.hypot(Math.max(x, rootView.getWidth() - x),
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
      @Override
      public void onAnimationEnd(Animator animation) {
        toolbar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        fab.setImageDrawable(
            AppCompatResources.getDrawable(getActivity(), R.drawable.ic_clear_white_24dp));
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
      @Override
      public void onAnimationStart(Animator animation) {
        toolbar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        menuLayout.setVisibility(View.GONE);
        fab.setImageDrawable(
            AppCompatResources.getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
      }
    });
    revealAnim.start();
  }

  @OnClick(R.id.arc_menu_ask_question)
  void askQuestion() {
    Controller controller = EditorController2.create(EditorType.ASK_QUESTION);
    controller.addLifecycleListener(new LifecycleListener() {
      @Override
      public void onChangeEnd(@NonNull Controller controller,
          @NonNull ControllerChangeHandler changeHandler,
          @NonNull ControllerChangeType changeType) {
        menuLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        fab.setSelected(false);
        fab.setImageDrawable(
            AppCompatResources.getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
      }
    });

    startTransaction(controller, new VerticalChangeHandler());
  }

  @OnClick(R.id.arc_menu_write_memory)
  void writeMemory() {
    Controller controller = EditorController.create(EditorType.BLOG);
    controller.addLifecycleListener(new LifecycleListener() {
      @Override
      public void onChangeEnd(@NonNull Controller controller,
          @NonNull ControllerChangeHandler changeHandler,
          @NonNull ControllerChangeType changeType) {
        menuLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        fab.setSelected(false);
        fab.setImageDrawable(
            AppCompatResources.getDrawable(getActivity(), R.drawable.ic_add_black_24dp));
      }
    });

    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override
  public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      adapter.updatePost(action, (PostRealm) payload);
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
          () -> startTransaction(ProfileController.create(me.getId()), new VerticalChangeHandler()),
          400);
    });
    tvUsername.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new VerticalChangeHandler()),
          400);
    });
    tvRealname.setOnClickListener(v -> {
      drawerLayout.closeDrawer(GravityCompat.START);
      handler.postDelayed(
          () -> startTransaction(ProfileController.create(me.getId()), new VerticalChangeHandler()),
          400);
    });
  }

  private void setupRecyclerView() {
    adapter = new FeedHomeAdapter(getActivity(), Glide.with(getActivity()));

    adapter.setOnRecommendedGroupHeaderClickListener(
        v -> startTransaction(GroupListController.create(), new VerticalChangeHandler()));
    adapter.setOnRecommendedGroupItemClickListener((v, model, item) -> {
      GroupController controller = GroupController.create(item.getId());
      startTransaction(controller, new VerticalChangeHandler());
    });

    adapter.setOnTrendingBlogHeaderClickListener(
        v -> startTransaction(BlogListController.create(), new VerticalChangeHandler()));
    adapter.setOnTrendingBlogItemClickListener((v, model, item) -> {
      // TODO: 9.04.2017 Implement
    });

    adapter.setOnBountyButtonClickListener(v -> {
      PostListController controller = PostListController.ofBounty();
      controller.setModelUpdateEvent(this);
      startTransaction(controller, new VerticalChangeHandler());
    });

    adapter.setOnPostClickListener(this);
    adapter.setOnProfileClickListener(this);
    adapter.setOnPostOptionsClickListener(this);
    adapter.setOnBookmarkClickListener(this);
    adapter.setOnContentImageClickListener(this);
    adapter.setOnShareClickListener(this);
    adapter.setOnCommentClickListener(this);
    adapter.setOnVoteClickListener(this);

    adapter.setOnNewcomersHeaderClickListener(v -> {
      // TODO: 9.04.2017 Implement
    });
    adapter.setOnNewcomersFollowClickListener((v, model, account, direction) -> {
      // TODO: 9.04.2017 Implement
    });
    adapter.setOnNewcomersItemClickListener((v, model, item) -> {
      // TODO: 9.04.2017 Implement
    });

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(lm);
    rvFeed.addItemDecoration(new SpaceItemDecoration(12, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(adapter);

    rvFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener(5) {
      @Override
      public void onLoadMore() {
        /*handler.postDelayed(
            () -> getPresenter().loadPosts(true, UUID.randomUUID().toString(), eTag, 20), 700);*/
      }
    });

    rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
    ab.setTitle(feedToolbarTitleString);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  @Override
  public void onSearchViewShown() {
    Toast.makeText(getActivity(), "onSearchViewShown", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onSearchViewClosed() {
    Toast.makeText(getActivity(), "onSearchViewClosed", Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    Toast.makeText(getActivity(), "onQueryTextSubmit: " + query, Toast.LENGTH_SHORT).show();
    return false;
  }

  @Override
  public void onQueryTextChange(String s) {

  }

  private void onInviteClicked() {
    Resources res = getResources();

    Intent intent = new AppInviteInvitation.IntentBuilder(res.getString(R.string.invitation_title))
        .setMessage(res.getString(R.string.invitation_message))
        .setDeepLink(Uri.parse(res.getString(R.string.invitation_deep_link)))
        .setCustomImage(Uri.parse(res.getString(R.string.invitation_custom_image)))
        .setCallToActionText(res.getString(R.string.invitation_cta))
        .build();
    startActivityForResult(intent, REQUEST_INVITE);
  }

  private void setupGoogleApi() {
    googleApiClient = new GoogleApiClient.Builder(getActivity())
        .addApi(AppInvite.API)
        .enableAutoManage((FragmentActivity) getActivity(), GoogleApiHelper.getSafeAutoManageId(),
            this)
        .build();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }
}
