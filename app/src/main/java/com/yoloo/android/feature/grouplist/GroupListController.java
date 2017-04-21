package com.yoloo.android.feature.grouplist;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.widget.StateLayout;
import java.util.List;
import timber.log.Timber;

public class GroupListController extends MvpController<GroupListView, GroupListPresenter>
    implements GroupListView, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener<GroupRealm>,
    GroupListAdapter.OnSubscribeListener {

  @BindView(R.id.root_view) StateLayout stateLayout;
  @BindView(R.id.recycler_view) RecyclerView rvGroupList;
  @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private GroupListAdapter adapter;

  public static GroupListController create() {
    return new GroupListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_group_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupToolbar();
    setupRecyclerview();
    swipeRefreshLayout.setOnRefreshListener(this);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getPresenter().loadGroups(false);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {
    if (!pullToRefresh) {
      stateLayout.setState(StateLayout.VIEW_STATE_LOADING);
    }
  }

  @Override
  public void onLoaded(List<GroupRealm> value) {
    adapter.addGroups(value);
    stateLayout.setState(StateLayout.VIEW_STATE_CONTENT);
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onError(Throwable e) {
    stateLayout.setState(StateLayout.VIEW_STATE_ERROR);
    swipeRefreshLayout.setRefreshing(false);
    Timber.e(e);
  }

  @Override
  public void onEmpty() {
    stateLayout.setState(StateLayout.VIEW_STATE_EMPTY);
    swipeRefreshLayout.setRefreshing(false);
  }

  @NonNull
  @Override
  public GroupListPresenter createPresenter() {
    return new GroupListPresenter(GroupRepositoryProvider.getRepository());
  }

  @Override
  public void onRefresh() {
    adapter.clear();
    getPresenter().loadGroups(true);
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, GroupRealm item) {
    RouterTransaction transaction = RouterTransaction.with(GroupController.create(item.getId()))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler());

    getRouter().pushController(transaction);
  }

  @Override
  public void onSubscribe(@NonNull String groupId, boolean subscribed) {
    if (subscribed) {
      getPresenter().unsubscribe(groupId);
    } else {
      getPresenter().subscribe(groupId);
    }
  }

  private void setupRecyclerview() {
    adapter = new GroupListAdapter(Glide.with(getActivity()), getActivity());
    adapter.setOnItemClickListener(this);
    adapter.setOnSubscribeClickListener(this);

    rvGroupList.setHasFixedSize(true);
    rvGroupList.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvGroupList.addItemDecoration(
        new DividerItemDecoration(getActivity(), OrientationHelper.VERTICAL));
    rvGroupList.setItemAnimator(new DefaultItemAnimator());
    rvGroupList.setAdapter(adapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setTitle("Groups");
  }
}
