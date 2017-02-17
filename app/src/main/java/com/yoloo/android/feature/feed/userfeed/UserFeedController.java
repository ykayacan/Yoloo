package com.yoloo.android.feature.feed.userfeed;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.lottie.LottieAnimationView;
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
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.LceAnimator;
import com.yoloo.android.feature.category.MainCatalogController;
import com.yoloo.android.feature.chat.conversationlist.ConversationListController;
import com.yoloo.android.feature.comment.CommentController;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.event.AcceptedEvent;
import com.yoloo.android.feature.feed.common.event.PostDeleteEvent;
import com.yoloo.android.feature.feed.common.event.UpdateEvent;
import com.yoloo.android.feature.feed.common.event.WriteNewPostEvent;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
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
import com.yoloo.android.feature.write.EditorType;
import com.yoloo.android.feature.write.catalog.CatalogController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.localmesaagemanager.LocalMessage;
import com.yoloo.android.localmesaagemanager.LocalMessageCallback;
import com.yoloo.android.localmesaagemanager.LocalMessageManager;
import com.yoloo.android.ui.changehandler.CircularRevealChangeHandler;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.fabmenu.FloatingActionsMenu;
import com.yoloo.android.ui.widget.materialbadge.MenuItemBadge;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.MenuHelper;
import com.yoloo.android.util.RxBus;
import com.yoloo.android.util.ShareUtil;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.WeakHandler;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class UserFeedController extends MvpController<UserFeedView, UserFeedPresenter>
    implements UserFeedView, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener, OnProfileClickListener,
    OnPostOptionsClickListener, OnReadMoreClickListener, OnShareClickListener,
    OnCommentClickListener, FeedAdapter.OnBountyClickListener,
    OnVoteClickListener, OnContentImageClickListener, LocalMessageCallback,
    OnItemClickListener<CategoryRealm> {

  @BindView(R.id.drawer_layout_feed) DrawerLayout drawerLayout;
  @BindView(R.id.nav_view_feed) NavigationView navigationView;
  @BindView(R.id.toolbar_feed) Toolbar toolbar;
  @BindView(R.id.rv_feed) RecyclerView rvFeed;
  @BindView(R.id.swipe_feed) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.view_feed_action) View actionView;
  @BindView(R.id.dimming_view) View dimmingView;
  @BindView(R.id.fab_menu_feed) FloatingActionsMenu fabMenu;
  @BindView(R.id.feed_root) ViewGroup root;
  @BindView(R.id.error_view) TextView errorView;
  @BindView(R.id.animation_view) LottieAnimationView lottieAnimationView;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;

  @BindInt(android.R.integer.config_mediumAnimTime) int mediumAnimTime;
  @BindInt(android.R.integer.config_longAnimTime) int longAnimTime;

  private FeedAdapter adapter;

  private WeakHandler handler;

  private String cursor;
  private String eTag;

  private Disposable disposable;

  private String userId;

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
    setupFab();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    handler = new WeakHandler();

    LocalMessageManager.getInstance().addListener(this);
    listenEventChanges();
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    LocalMessageManager.getInstance().removeListener(this);
    disposable.dispose();
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
        startTransaction(new ConversationListController(), new VerticalChangeHandler());
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
    adapter.addTrendingCategories(topics, this);

    NewsRealm n1 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Interrailde Büyük Gelişme")
        .setCover(true)
        .setBgImageUrl("https://www.uzakrota.com/wp-content/uploads/2017/02/edinburgh-730x548.jpg");

    NewsRealm n2 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Vizeler Kalktı")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/mercator-Accelya-warburg-pincus-airline-services-730x548.jpg");

    NewsRealm n3 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Gezginler Evi Burada Açılıyor")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/Airbnb-helps-combat-winter-blues-with-magical-green-wonderland-in-London-730x492.jpg");

    NewsRealm n4 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Yeni Yerler Keşfetmenin Tam Zamanı")
        .setBgImageUrl(
            "http://webneel.com/daily/sites/default/files/images/daily/10-2013/1-travel-photography.preview.jpg");

    List<NewsRealm> list = new ArrayList<>();
    list.add(n1);
    list.add(n2);
    list.add(n3);
    list.add(n4);

    adapter.addNews(list, (v, model, item) -> Timber.d("News: %s", item.getTitle()));
  }

  @Override public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      LceAnimator.showLoading(lottieAnimationView, swipeRefreshLayout, errorView);
    }
  }

  @Override public void onLoaded(Response<List<PostRealm>> value) {
    cursor = value.getCursor();
    eTag = value.geteTag();

    adapter.addPosts(value.getData());
  }

  @Override public void showContent() {
    LceAnimator.showContent2(lottieAnimationView, swipeRefreshLayout, errorView);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
    Timber.e(e);
  }

  @Override public void onEmpty() {
    actionView.setVisibility(View.VISIBLE);
  }

  @Override public void onRefresh() {
    adapter.clear();

    getPresenter().loadPosts(true, cursor, eTag, 20);
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
        handler.postDelayed(
            () -> startTransaction(PostController.ofBookmarked(), new VerticalChangeHandler()),
            400);
        return false;
      case R.id.action_nav_settings:
        AuthUI.getInstance().signOut(getActivity());
        handler.postDelayed(() -> getRouter().setRoot(RouterTransaction.with(new AuthController())
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
    return super.handleBack();
  }

  @Override public void handleMessage(@NonNull LocalMessage msg) {
    switch (msg.getId()) {
      case R.integer.message_create_new_post:
        msg.getObject();
        rvFeed.smoothScrollToPosition(0);
        adapter.addPostToBeginning((PostRealm) msg.getObject());
        break;
    }
  }

  @NonNull @Override public UserFeedPresenter createPresenter() {
    return new UserFeedPresenter(
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()),
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onCommentClick(View v, PostRealm post) {
    startTransaction(CommentController.create(post), new FadeChangeHandler());
  }

  @Override
  public void onPostOptionsClick(View v, EpoxyModel<?> model, PostRealm post) {
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

  @Override public void onProfileClick(View v, String ownerId) {
    startTransaction(ProfileController.create(ownerId), new VerticalChangeHandler());
  }

  @Override public void onReadMoreClickListener(View v, PostRealm post) {
    startTransaction(PostDetailController.create(post.getId()), new FadeChangeHandler());
  }

  @Override public void onShareClick(View v, PostRealm post) {
    ShareUtil.share(this, null, post.getContent());
  }

  @Override public void onBountyClick(View v) {
    startTransaction(PostController.ofBounty(), new VerticalChangeHandler());
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, CategoryRealm item) {
    startTransaction(PostController.ofCategory(item.getName()), new VerticalChangeHandler());
  }

  @Override public void onVoteClick(String votableId, int direction, @Type int type) {
    getPresenter().votePost(votableId, direction);
  }

  @Override public void onContentImageClick(View v, String url) {
    startTransaction(PhotoController.create(url), new FadeChangeHandler());
  }

  @OnClick(R.id.fab_ask_question) void openAskQuestion() {
    fabMenu.collapseImmediately();

    AnimatorChangeHandler changeHandler = VersionUtil.hasL()
        ? new CircularRevealChangeHandler(fabMenu, root, mediumAnimTime)
        : new VerticalChangeHandler();

    startTransaction(CatalogController.create(EditorType.ASK_QUESTION), changeHandler);
  }

  @OnClick(R.id.fab_share_trip) void openShareTrip() {
    fabMenu.collapseImmediately();

    AnimatorChangeHandler changeHandler = VersionUtil.hasL()
        ? new CircularRevealChangeHandler(fabMenu, root, mediumAnimTime)
        : new VerticalChangeHandler(mediumAnimTime);

    startTransaction(CatalogController.create(EditorType.SHARE_TRIP), changeHandler);
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
          .bitmapTransform(new CropCircleTransformation(getActivity()))
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
    adapter = FeedAdapter.builder(getActivity())
        .isMainFeed(true)
        .onProfileClickListener(this)
        .onBountyClickListener(this)
        .onCommentClickListener(this)
        .onContentImageClickListener(this)
        .onTrendingCategoryHeaderClickListener(
            v -> startTransaction(new MainCatalogController(), new FadeChangeHandler()))
        .onTravelNewsHeaderClickListener(
            v -> startTransaction(new MainCatalogController(), new VerticalChangeHandler()))
        .onOptionsClickListener(this)
        .onReadMoreClickListener(this)
        .onVoteClickListener(this)
        .onShareClickListener(this)
        .build();

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    lm.setInitialPrefetchItemCount(5);

    rvFeed.setLayoutManager(lm);
    rvFeed.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    animator.setAddDuration(0L);
    rvFeed.setItemAnimator(animator);

    rvFeed.setHasFixedSize(true);
    rvFeed.setAdapter(adapter);

    rvFeed.addOnScrollListener(new EndlessRecyclerOnScrollListener(5) {
      @Override public void onLoadMore() {
        handler.postDelayed(
            () -> getPresenter().loadPosts(true, UUID.randomUUID().toString(), eTag, 20), 700);
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

  private void setupFab() {
    fabMenu.setupWithDimmingView(dimmingView,
        ContextCompat.getColor(getActivity(), R.color.fab_dim));
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void listenEventChanges() {
    disposable = RxBus.get().observeEvents(getClass())
        .subscribe(e -> {
          if (e instanceof UpdateEvent) {
            adapter.updatePost(FeedAction.UPDATE, ((UpdateEvent) e).getPost());
          } else if (e instanceof PostDeleteEvent) {
            adapter.updatePost(FeedAction.DELETE, ((PostDeleteEvent) e).getPost());
          } else if (e instanceof WriteNewPostEvent) {
            rvFeed.smoothScrollToPosition(0);
            handler.postDelayed(
                () -> adapter.addPostToBeginning(((WriteNewPostEvent) e).getPost()), 450);
          } else if (e instanceof AcceptedEvent) {
            adapter.updatePost(FeedAction.UPDATE, ((AcceptedEvent) e).getPost());
          }
        });
  }
}