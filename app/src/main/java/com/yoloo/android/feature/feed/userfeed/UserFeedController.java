package com.yoloo.android.feature.feed.userfeed;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
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
import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.CategoryFaker;
import com.yoloo.android.data.faker.CommentFaker;
import com.yoloo.android.data.faker.NotificationFaker;
import com.yoloo.android.data.faker.PostFaker;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.BaseActivity;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.category.MainCatalogController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.fcm.FCMListener;
import com.yoloo.android.feature.fcm.FCMManager;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.event.DeleteEvent;
import com.yoloo.android.feature.feed.common.event.UpdateEvent;
import com.yoloo.android.feature.feed.common.event.WriteNewPostEvent;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.postfeed.PostController;
import com.yoloo.android.feature.login.AuthController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.feature.ui.CircularRevealChangeHandler;
import com.yoloo.android.feature.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import com.yoloo.android.feature.ui.widget.floatingactionmenu.widget.FloatingActionMenu;
import com.yoloo.android.feature.ui.widget.materialbadge.MenuItemBadge;
import com.yoloo.android.feature.write.EditorType;
import com.yoloo.android.feature.write.catalog.CatalogController;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.NotificationHelper;
import com.yoloo.android.util.RxBus;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.CropCircleTransformation;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

public class UserFeedController extends MvpController<UserFeedView, UserFeedPresenter>
    implements UserFeedView, SwipeRefreshLayout.OnRefreshListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener, OnOptionsClickListener,
    OnReadMoreClickListener, OnShareClickListener, OnCommentClickListener,
    FeedAdapter.OnBountyClickListener, FeedAdapter.OnCategoryClickListener,
    FeedAdapter.OnExploreCategoriesClickListener, OnVoteClickListener, OnContentImageClickListener,
    FCMListener {

  static {
    CategoryFaker.generate();
    PostFaker.generate();
    CommentFaker.generate();
    NotificationFaker.generate();
  }

  @BindView(R.id.drawer_layout_feed) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view_feed) NavigationView navigationView;
  @BindView(R.id.toolbar_feed) Toolbar toolbar;
  @BindView(R.id.rv_feed) RecyclerView rvFeed;
  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.view_feed_action) View actionView;
  @BindView(R.id.dimming_view) View dimmingView;
  @BindView(R.id.fab_menu_feed) FloatingActionMenu fabMenu;
  @BindView(R.id.feed_root) ViewGroup root;

  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;

  private FeedAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  private WeakHandler handler = new WeakHandler();

  private boolean hasNotification;

  private String cursor;
  private String eTag;

  private Disposable disposable;

  private String userId;

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
    setupFab();

    FCMManager.getInstance(getApplicationContext()).register(this);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    rvFeed.addOnScrollListener(endlessRecyclerViewScrollListener);

    disposable = RxBus.get().observeEvents(getClass())
        .subscribe(e -> {
          if (e instanceof UpdateEvent) {
            adapter.updatePost(FeedAction.UPDATE, ((UpdateEvent) e).getPost());
          } else if (e instanceof DeleteEvent) {
            adapter.updatePost(FeedAction.DELETE, ((DeleteEvent) e).getPost());
          } else if (e instanceof WriteNewPostEvent) {
            rvFeed.smoothScrollToPosition(0);
            handler.postDelayed(
                () -> adapter.addPostAfterBountyButton(((WriteNewPostEvent) e).getPost()), 450);
          }
        });
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvFeed.removeOnScrollListener(endlessRecyclerViewScrollListener);
    disposable.dispose();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    FCMManager.getInstance(getApplicationContext()).unRegister();
  }

  @Override protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeStarted(changeHandler, changeType);
    if (changeType.equals(ControllerChangeType.PUSH_EXIT)) {
      disposable.dispose();
      RxBus.get().clear();
    }
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_feed_user, menu);

    DrawableHelper.withContext(getActivity()).withColor(android.R.color.white).applyTo(menu);

    MenuItem menuMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuMessage, new MenuItemBadge.Builder());
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_feed_search:
        startTransaction(new SearchController(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_message:
        if (hasNotification) {
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
        }
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
  }

  @Override public void onTrendingCategoriesLoaded(List<CategoryRealm> topics) {
    adapter.addTrendingCategories(topics);
  }

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.showLoadMoreIndicator(false);
    endlessRecyclerViewScrollListener.setProgressBarVisible(false);
    adapter.addPosts(value.getData());
  }

  @Override public void onError(Throwable e) {
    Timber.e(e);
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
    adapter.showLoadMoreIndicator(true);
    endlessRecyclerViewScrollListener.setProgressBarVisible(true);
    handler.postDelayed(() -> getPresenter().loadFeed(false, cursor, eTag, 20), 700);
  }

  @NonNull @Override public UserFeedPresenter createPresenter() {
    return new UserFeedPresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()),
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()),
        NotificationRepository.getInstance(
            NotificationRemoteDataSource.getInstance(),
            NotificationDiskDataSource.getInstance()),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case R.id.action_nav_profile:
        drawerLayout.closeDrawer(GravityCompat.START);
        handler.postDelayed(
            () -> startTransaction(ProfileController.create(userId), new VerticalChangeHandler()),
            400);
        return false;
      case R.id.action_nav_settings:
        AuthUI.getInstance().signOut(getActivity());
        drawerLayout.closeDrawer(GravityCompat.START);
        getRouter().setRoot(RouterTransaction.with(new AuthController())
            .pushChangeHandler(new FadeChangeHandler()));
        return true;
      default:
        return false;
    }
  }

  @Override public boolean handleBack() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
      return true;
    }
    return super.handleBack();
  }

  @Override public void onCommentClick(View v, String itemId, String acceptedCommentId) {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    startTransaction(CommentController.create(itemId), new VerticalChangeHandler());
  }

  @Override
  public void onOptionsClick(View v, EpoxyModel<?> model, String postId, String postOwnerId) {
    final PopupMenu optionsMenu = MenuHelper.createMenu(getActivity(), v, R.menu.menu_post_popup);
    final boolean self = userId.equals(postOwnerId);
    optionsMenu.getMenu().getItem(1).setVisible(self);
    optionsMenu.getMenu().getItem(2).setVisible(self);

    optionsMenu.setOnMenuItemClickListener(item -> {
      final int itemId = item.getItemId();
      switch (itemId) {
        case R.id.action_feed_popup_bookmark:
          getPresenter().bookmarkPost(postId);
          Snackbar.make(getView(), R.string.label_feed_bookmarked, Snackbar.LENGTH_SHORT).show();
          return true;
        case R.id.action_feed_popup_edit:
          return true;
        case R.id.action_feed_popup_delete:
          getPresenter().deletePost(postId);
          adapter.delete(model);
          return true;
        default:
          return false;
      }
    });
  }

  @Override public void onProfileClick(View v, String ownerId) {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    startTransaction(ProfileController.create(ownerId), new VerticalChangeHandler());
  }

  @Override public void onReadMoreClickListener(View v, String postId) {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    startTransaction(PostDetailController.create(postId), new VerticalChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onBountyClick(View v) {
    setRetainViewMode(RetainViewMode.RELEASE_DETACH);
    startTransaction(PostController.ofBounty(), new VerticalChangeHandler());
  }

  @Override public void onCategoryClick(View v, String categoryId, String name) {
    setRetainViewMode(RetainViewMode.RELEASE_DETACH);
    startTransaction(PostController.ofCategory(name), new VerticalChangeHandler());
  }

  @Override public void onExploreCategoriesClick(View v) {
    setRetainViewMode(RetainViewMode.RELEASE_DETACH);
    startTransaction(new MainCatalogController(), new VerticalChangeHandler());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override public void onContentImageClick(View v, String url) {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    startTransaction(PhotoController.create(url), new FadeChangeHandler());
  }

  @OnClick(R.id.fab_ask_question) void openAskQuestion() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    fabMenu.collapseImmediately();

    AnimatorChangeHandler handler = VersionUtil.hasL()
        ? new CircularRevealChangeHandler(fabMenu, root, mediumAnimTime)
        : new VerticalChangeHandler(mediumAnimTime);

    startTransaction(CatalogController.create(EditorType.ASK_QUESTION), handler);
  }

  @OnClick(R.id.fab_share_trip) void openShareTrip() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    fabMenu.collapseImmediately();

    AnimatorChangeHandler handler = VersionUtil.hasL()
        ? new CircularRevealChangeHandler(fabMenu, root, mediumAnimTime)
        : new VerticalChangeHandler(mediumAnimTime);

    startTransaction(CatalogController.create(EditorType.SHARE_TRIP), handler);
  }

  @Override public void onDeviceRegistered(String deviceToken) {
    Timber.d("Device token: %s", deviceToken);
    getPresenter().registerFcmToken(deviceToken);
  }

  @Override public void onMessage(RemoteMessage remoteMessage) {
    sendNotification(remoteMessage.getData());
  }

  @Override public void onPlayServiceError() {

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

    if (user != null) {
      Glide.with(getActivity())
          .load(user.getPhotoUrl())
          .bitmapTransform(CropCircleTransformation.getInstance(getActivity()))
          .into(ivNavAvatar);

      tvRealname.setText(user.getDisplayName());
      tvUsername.setText(user.getDisplayName().trim().replace(" ", "").toLowerCase());
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
    adapter = FeedAdapter.builder()
        .isMainFeed(true)
        .context(getApplicationContext())
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

    rvFeed.setHasFixedSize(false);
    rvFeed.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.primary));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(getResources().getString(R.string.label_feed_toolbar_title));
    }
  }

  private void setupFab() {
    fabMenu.setupWithDimmingView(dimmingView,
        ContextCompat.getColor(getActivity(), R.color.fab_dim));
  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param data FCM message body received.
   */
  private void sendNotification(Map<String, String> data) {
    final String contentText = NotificationHelper.getRelatedNotificationString(getActivity(), data);

    Intent intent = new Intent(getActivity(), BaseActivity.class);
    intent.putExtra(BaseActivity.KEY_ACTION, data.get("action"));
    intent.putExtra(BaseActivity.KEY_DATA, new HashMap<>(data));

    PendingIntent pendingIntent =
        PendingIntent.getActivity(getActivity(), 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(getActivity()).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Yoloo")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

    NotificationManager notificationManager =
        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0 /* ID get notification */, notificationBuilder.build());
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}