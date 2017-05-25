package com.yoloo.android.feature.group.groupuserslist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.yoloo.android.ui.widget.StateLayout;
import com.yoloo.android.util.BundleBuilder;
import java.util.List;
import timber.log.Timber;

public class GroupUsersListController
    extends MvpController<GroupUsersListView, GroupUsersListPresenter>
    implements GroupUsersListView, OnProfileClickListener, OnFollowClickListener {

  private static final String KEY_GROUP_ID = "GROUP_ID";

  @BindView(R.id.root_view) StateLayout stateLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView rvUsers;

  @BindString(R.string.user_followed) String userFollowedString;
  @BindString(R.string.user_unfollowed) String userUnfollowedString;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private String groupId;

  private boolean reEnter;

  private GroupUsersListEpoxyController epoxyController;

  public GroupUsersListController(@Nullable Bundle args) {
    super(args);
  }

  public static GroupUsersListController create(@NonNull String groupId) {
    Bundle bundle = new BundleBuilder().putString(KEY_GROUP_ID, groupId).build();

    return new GroupUsersListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_group_users_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupRecyclerview();
    setupToolbar();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    groupId = getArgs().getString(KEY_GROUP_ID);

    if (!reEnter) {
      getPresenter().loadUsers(groupId, false);
      reEnter = true;
    }
  }

  @NonNull
  @Override
  public GroupUsersListPresenter createPresenter() {
    return new GroupUsersListPresenter(GroupRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    stateLayout.setState(StateLayout.VIEW_STATE_LOADING);
  }

  @Override
  public void onLoaded(List<AccountRealm> value) {
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

  @Override
  public void onProfileClick(View v, String userId) {
    RouterTransaction transaction = RouterTransaction
        .with(ProfileController.create(userId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler());

    getRouter().pushController(transaction);
  }

  @Override
  public void onFollowClick(View v, AccountRealm account, int direction) {
    if (direction == 1) {
      getPresenter().follow(account.getId());
    } else if (direction == -1) {
      getPresenter().unfollow(account.getId());
    }
  }

  @Override
  public void onFollowedSuccessfully() {
    showSnackbar(userFollowedString);
  }

  @Override
  public void onUnfollowedSuccessfully() {
    showSnackbar(userUnfollowedString);
  }

  @Override public void onMoreDataLoaded(List<AccountRealm> accounts) {
    if (accounts.isEmpty()) {
      epoxyController.hideLoader();
    } else {
      epoxyController.setLoadMoreData(accounts);
    }
  }

  private void setupRecyclerview() {
    epoxyController = new GroupUsersListEpoxyController(this, this);

    LinearLayoutManager lm = new LinearLayoutManager(getActivity());
    rvUsers.setLayoutManager(lm);
    rvUsers.addItemDecoration(new DividerItemDecoration(getActivity(), OrientationHelper.VERTICAL));
    rvUsers.setAdapter(epoxyController.getAdapter());
    rvUsers.setHasFixedSize(true);

    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(lm) {
          @Override public void onLoadMore(int totalItemsCount, RecyclerView view) {
            getPresenter().loadUsers(groupId, true);
            epoxyController.showLoader();
          }
        };
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.group_tab_users);
  }

  private void showSnackbar(String message) {
    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
  }
}
