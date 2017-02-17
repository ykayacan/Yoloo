package com.yoloo.android.feature.follow;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import timber.log.Timber;

public class FollowController extends MvpController<FollowView, FollowPresenter>
    implements FollowView, OnProfileClickListener, OnFollowClickListener,
    SwipeRefreshLayout.OnRefreshListener, EndlessRecyclerViewScrollListener.OnLoadMoreListener {

  public static final int TYPE_FOLLOWERS = 0;
  public static final int TYPE_FOLLOWINGS = 1;

  private static final String KEY_VIEW_TYPE = "VIEW_TYPE";
  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.rv_follow) RecyclerView rvFollow;
  @BindView(R.id.swipe_follow) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar_follow) Toolbar toolbar;

  @BindColor(R.color.primary) int colorPrimary;

  private FollowAdapter adapter;

  private String cursor;
  private String eTag;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public FollowController(@Nullable Bundle args) {
    super(args);
  }

  public static FollowController create(String userId, @FollowViewType int viewType) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putInt(KEY_VIEW_TYPE, viewType)
        .build();

    return new FollowController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_follow, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupToolbar();
    setHasOptionsMenu(true);
    setupPullToRefresh();
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    final Bundle args = getArgs();

    final String userId = args.getString(KEY_USER_ID);
    final int viewType = args.getInt(KEY_VIEW_TYPE);

    if (viewType == TYPE_FOLLOWERS) {
      getPresenter().loadFollowers(false, userId, cursor, eTag, 20);
    } else {
      getPresenter().loadFollowings(false, userId, cursor, eTag, 20);
    }

    rvFollow.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvFollow.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override public void onLoaded(Response<List<AccountRealm>> value) {
    swipeRefreshLayout.setRefreshing(false);

    cursor = value.getCursor();
    eTag = value.geteTag();
    adapter.addUsers(value.getData());
  }

  @Override public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void onEmpty() {

  }

  @Override public void onRefresh() {
    endlessRecyclerViewScrollListener.resetState();
    adapter.clear();
  }

  @Override public void onLoadMore() {
    Timber.d("onLoadMore");
  }

  @NonNull @Override public FollowPresenter createPresenter() {
    return new FollowPresenter(UserRepository.getInstance(
        UserRemoteDataStore.getInstance(),
        UserDiskDataStore.getInstance()));
  }

  @Override public void onProfileClick(View v, String ownerId) {
    getRouter().pushController(RouterTransaction.with(ProfileController.create(ownerId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onFollowClick(View v, String userId, int direction) {
    getPresenter().follow(userId, direction);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final int viewType = getArgs().getInt(KEY_VIEW_TYPE);

    // addPostToBeginning back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(viewType == TYPE_FOLLOWERS ? R.string.label_follow_followers_title
          : R.string.label_follow_following_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupRecyclerView() {
    adapter = new FollowAdapter(getActivity(), this, this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvFollow.setLayoutManager(layoutManager);
    rvFollow.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFollow.setItemAnimator(animator);

    rvFollow.setHasFixedSize(true);
    rvFollow.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, this);
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(colorPrimary);
  }

  @IntDef({
      FollowController.TYPE_FOLLOWERS,
      FollowController.TYPE_FOLLOWINGS
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface FollowViewType {
  }
}
