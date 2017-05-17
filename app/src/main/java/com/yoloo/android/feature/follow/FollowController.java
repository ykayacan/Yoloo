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
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ViewUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class FollowController extends MvpController<FollowView, FollowPresenter>
    implements FollowView, OnProfileClickListener, OnFollowClickListener,
    SwipeRefreshLayout.OnRefreshListener {

  public static final int TYPE_FOLLOWERS = 0;
  public static final int TYPE_FOLLOWINGS = 1;

  private static final String KEY_VIEW_TYPE = "VIEW_TYPE";
  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.recycler_view) RecyclerView rvFollow;
  @BindView(R.id.swipe_follow) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindColor(R.color.primary) int colorPrimary;
  @BindColor(R.color.primary_dark) int colorPrimaryDark;

  private String userId;
  private int viewType;

  private FollowEpoxyController epoxyController;

  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  public FollowController(@Nullable Bundle args) {
    super(args);
  }

  public static FollowController create(String userId, @FollowViewType int viewType) {
    final Bundle bundle =
        new BundleBuilder().putString(KEY_USER_ID, userId).putInt(KEY_VIEW_TYPE, viewType).build();

    return new FollowController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_follow, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupToolbar();
    setupPullToRefresh();
    setupRecyclerView();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    final Bundle args = getArgs();

    userId = args.getString(KEY_USER_ID);
    viewType = args.getInt(KEY_VIEW_TYPE);

    if (viewType == TYPE_FOLLOWERS) {
      getPresenter().loadFollowers(false, false, userId);
    } else {
      getPresenter().loadFollowings(false, false, userId);
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), colorPrimaryDark);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    swipeRefreshLayout.setRefreshing(pullToRefresh);
  }

  @Override
  public void onLoaded(List<AccountRealm> value) {
    swipeRefreshLayout.setRefreshing(false);
    epoxyController.setData(value, false);
  }

  @Override
  public void onError(Throwable e) {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onEmpty() {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onRefresh() {
    endlessRecyclerOnScrollListener.resetState();

    if (viewType == TYPE_FOLLOWERS) {
      getPresenter().loadFollowers(true, true, userId);
    } else {
      getPresenter().loadFollowings(true, true, userId);
    }
  }

  @Override public void onLoadedMore(List<AccountRealm> accounts) {
    if (accounts.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(accounts);
    }

    swipeRefreshLayout.setRefreshing(false);
  }

  @NonNull
  @Override
  public FollowPresenter createPresenter() {
    return new FollowPresenter(UserRepositoryProvider.getRepository());
  }

  @Override
  public void onProfileClick(View v, String userId) {
    getRouter().pushController(RouterTransaction
        .with(ProfileController.create(userId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override
  public void onFollowClick(View v, AccountRealm account, int direction) {
    getPresenter().follow(account.getId(), direction);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final int viewType = getArgs().getInt(KEY_VIEW_TYPE);

    // addPostToBeginning back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    ab.setTitle(viewType == TYPE_FOLLOWERS
        ? R.string.label_follow_followers_title
        : R.string.label_follow_following_title);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerView() {
    epoxyController = new FollowEpoxyController(getActivity(), this, this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvFollow.setLayoutManager(lm);
    rvFollow.addItemDecoration(new SpaceItemDecoration(8, SpaceItemDecoration.VERTICAL));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvFollow.setItemAnimator(animator);

    rvFollow.setHasFixedSize(true);
    rvFollow.setAdapter(epoxyController.getAdapter());

    endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(lm) {
      @Override public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
        if (viewType == TYPE_FOLLOWERS) {
          getPresenter().loadFollowers(false, true, userId);
        } else {
          getPresenter().loadFollowings(false, true, userId);
        }
      }
    };
  }

  private void setupPullToRefresh() {
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeColors(colorPrimary);
  }

  @IntDef({
      FollowController.TYPE_FOLLOWERS, FollowController.TYPE_FOLLOWINGS
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface FollowViewType {
  }
}
