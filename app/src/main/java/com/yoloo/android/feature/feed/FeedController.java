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
import android.support.v4.app.FragmentActivity;
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
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyModel;
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
import com.claudiodegio.msv.OnSearchViewListener;
import com.github.ag.floatingactionmenu.OptionsFabLayout;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.util.GoogleApiHelper;
import com.yoloo.android.feature.blog.BlogController;
import com.yoloo.android.feature.bloglist.BlogListController;
import com.yoloo.android.feature.chat.chatlist.ChatListController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.editor.editor.BlogEditorController;
import com.yoloo.android.feature.editor.editor.PostEditorController;
import com.yoloo.android.feature.editor.job.SendPostJob;
import com.yoloo.android.feature.explore.ExploreController;
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
import com.yoloo.android.feature.groupgridoverview.GroupGridOverviewController;
import com.yoloo.android.feature.models.newusers.NewUserListModelGroup;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.feature.settings.SettingsController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.ui.widget.materialbadgetextview.MenuItemBadge;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

public class FeedController extends MvpController<FeedView, FeedPresenter>
    implements FeedView, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnShareClickListener, OnCommentClickListener, OnVoteClickListener,
    OnContentImageClickListener, OnModelUpdateEvent, OnBookmarkClickListener,
    OnItemClickListener<PostRealm>, OnSearchViewListener,
    GoogleApiClient.OnConnectionFailedListener {

  private static final int REQUEST_INVITE = 0;

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

  @BindDrawable(R.drawable.ic_email_outline) Drawable emptyNotificationDrawable;
  @BindDrawable(R.drawable.ic_email_black_24dp) Drawable fullNotificationDrawable;

  @BindString(R.string.label_feed_toolbar_title) String feedToolbarTitleString;
  @BindString(R.string.app_name) String appNameString;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;
  @BindInt(android.R.integer.config_longAnimTime) int longAnimTime;

  private GoogleApiClient googleApiClient;

  private FeedEpoxyController epoxyController;

  private WeakHandler handler;

  private AccountRealm me;

  private MenuItem menuItemMessage;

  private TutoShowcase welcomeShowcase;
  private TutoShowcase fabShowcase;

  private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      PostRealm post = Parcels.unwrap(intent.getParcelableExtra(SendPostJob.KEY_ADD_POST));
      rvFeed.smoothScrollToPosition(3);
      handler.postDelayed(() -> epoxyController.addPost(post), 450);
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

    msv.setOnSearchViewListener(this);

    fab.setMainFabOnClickListener(v -> {
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
    showWelcomeTutorial();

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
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_feed_home, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);

    menuItemMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuItemMessage, new MenuItemBadge.Builder()
        .iconDrawable(emptyNotificationDrawable)
        .iconTintColor(Color.WHITE));

    /*MenuItem menuItemSearch = menu.findItem(R.id.action_feed_search);
    msv.setMenuItem(menuItemSearch);*/
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_notification:
        startTransaction(new NotificationController(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_search:
        startTransaction(SearchController.create(), new SimpleSwapChangeHandler());
        return true;
      case R.id.action_feed_message:
        /*MenuItemBadge.update(getActivity(), menuItemMessage, new MenuItemBadge.Builder()
            .iconDrawable(fullNotificationDrawable)
            .textBackgroundColor(Color.parseColor("#36b100"))
            .iconTintColor(Color.WHITE));
        MenuItemBadge.getBadgeTextView(menuItemMessage).setBadgeCount(2, true);*/

        getRouter().pushController(RouterTransaction
            .with(ChatListController.create(me.getId()))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler())
            .tag(ChatListController.TAG));
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
    addUserPhotoToToolbar(me);

    epoxyController.setUserId(me.getId());
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

  @Override
  public void onLoaded(List<FeedItem> items) {
    epoxyController.setData(items, false);
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
    epoxyController.onRefresh();
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
          emailIntent.setData(Uri.parse("mailto: hello@yoloo.com"));
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

    if (msv.isOpen()) {
      msv.closeSearch();
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
  public void onCommentClick(View v, PostRealm post) {
    CommentController controller = CommentController.create(post);
    controller.setModelUpdateEvent(this);
    startTransaction(controller, new VerticalChangeHandler());
  }

  @Override
  public void onPostOptionsClick(View v, PostRealm post) {
    final PopupMenu menu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);

    menu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      if (itemId == R.id.action_popup_delete) {
        getPresenter().deletePost(post.getId());
        epoxyController.deletePost(post);
        //adapter.delete(model);
        return true;
      }

      return super.onOptionsItemSelected(item);
    });
  }

  @Override
  public void onProfileClick(View v, String userId) {
    startTransaction(ProfileController.create(userId), new VerticalChangeHandler());
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
    startTransaction(FullscreenPhotoController.create(media.getLargeSizeUrl(), media.getId()),
        new FadeChangeHandler());
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

  @Override
  public void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload) {
    if (payload instanceof PostRealm) {
      //adapter.updatePost(action, (PostRealm) payload);
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
    epoxyController = new FeedEpoxyController(Glide.with(getActivity()), getActivity());

    epoxyController.setRecommendedGroupListCallbacks(
        new FeedEpoxyController.RecommendedGroupListCallbacks() {
          @Override
          public void onRecommendedGroupsHeaderClicked() {
            GroupGridOverviewController controller =
                GroupGridOverviewController.create(2, true, false);
            startTransaction(controller, new VerticalChangeHandler());
          }

          @Override
          public void onRecommendedGroupClicked(GroupRealm group) {
            GroupController controller = GroupController.create(group.getId());
            startTransaction(controller, new VerticalChangeHandler());
          }
        });

    epoxyController.setTrendingBlogListCallbacks(
        new FeedEpoxyController.TrendingBlogListCallbacks() {
          @Override
          public void onTrendingBlogHeaderClicked() {
            startTransaction(BlogListController.create(), new VerticalChangeHandler());
          }

          @Override
          public void onTrendingBlogClicked(PostRealm blog) {
            startTransaction(BlogController.create(blog), new HorizontalChangeHandler());
          }

          @Override
          public void onTrendingBlogBookmarkClicked(String postId, boolean bookmark) {
            if (bookmark) {
              getPresenter().bookmarkPost(postId);
            } else {
              getPresenter().unBookmarkPost(postId);
            }
          }

          @Override
          public void onTrendingBlogOptionsClicked(View v, PostRealm post) {
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
        });

    epoxyController.setOnBountyButtonClickListener(v -> {
      Timber.d("Bounty button click");
      PostListController controller = PostListController.ofBounty();
      controller.setModelUpdateEvent(this);
      startTransaction(controller, new VerticalChangeHandler());
    });

    epoxyController.setOnPostClickListener(this);
    epoxyController.setOnProfileClickListener(this);
    epoxyController.setOnPostOptionsClickListener(this);
    epoxyController.setOnBookmarkClickListener(this);
    epoxyController.setOnContentImageClickListener(this);
    epoxyController.setOnShareClickListener(this);
    epoxyController.setOnCommentClickListener(this);
    epoxyController.setOnVoteClickListener(this);

    epoxyController.setNewUserListModelGroupCallbacks(
        new NewUserListModelGroup.NewUserListModelGroupCallbacks() {
          @Override
          public void onNewUserListHeaderClicked() {

          }

          @Override
          public void onNewUserClicked(AccountRealm account) {
            startTransaction(ProfileController.create(account.getId()),
                new VerticalChangeHandler());
          }

          @Override
          public void onNewUserFollowClicked(AccountRealm account, int direction) {
            getPresenter().follow(account.getId(), direction);
            epoxyController.deleteNewUser(account);
          }
        });

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvFeed.setLayoutManager(lm);
    rvFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(epoxyController.getAdapter());

    /*rvFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener(5) {
      @Override
      public void onLoadMore() {
        Timber.d("onLoadMore()");
        getPresenter().loadMorePosts();
      }
    });*/
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
        .setMessage(res.getString(R.string.invitation_message, me.getUsername()))
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
}
