package com.yoloo.android.feature.groupgridoverview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.ViewUtils;
import java.util.List;
import timber.log.Timber;

public class GroupGridOverviewController
    extends MvpController<GroupGridOverviewView, GroupGridOverviewPresenter>
    implements GroupGridOverviewView, OnItemClickListener<GroupRealm> {

  private static final String KEY_SPAN_COUNT = "SPAN_COUNT";
  private static final String KEY_USER_ID = "USER_ID";
  private static final String KEY_ENABLE_TOOLBAR = "ENABLE_TOOLBAR";
  private static final String KEY_INCLUDE_EDGE = "INCLUDE_EDGE";

  @BindView(R.id.recycler_view) RecyclerView rvGroupGrid;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private int spanCount;

  private GroupGridEpoxyController epoxyController;

  public GroupGridOverviewController() {
  }

  public GroupGridOverviewController(Bundle args) {
    super(args);
  }

  public static GroupGridOverviewController create() {
    return new GroupGridOverviewController();
  }

  public static GroupGridOverviewController create(int spanCount, boolean enableToolbar,
      boolean includeEdge) {
    return create(null, spanCount, enableToolbar, includeEdge);
  }

  public static GroupGridOverviewController create(String userId, int spanCount) {
    return create(userId, spanCount, false, true);
  }

  public static GroupGridOverviewController create(String userId, int spanCount,
      boolean enableToolbar, boolean includeEdge) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putInt(KEY_SPAN_COUNT, spanCount)
        .putBoolean(KEY_ENABLE_TOOLBAR, enableToolbar)
        .putBoolean(KEY_INCLUDE_EDGE, includeEdge)
        .build();

    return new GroupGridOverviewController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_group_grid_overview, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    spanCount = getArgs().getInt(KEY_SPAN_COUNT, 3);

    final boolean enableToolbar = getArgs().getBoolean(KEY_ENABLE_TOOLBAR, false);
    toolbar.setVisibility(enableToolbar ? View.VISIBLE : View.GONE);
    if (enableToolbar) {
      setupToolbar();
    }

    setupRecyclerView();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    final String userId = getArgs().getString(KEY_USER_ID, null);

    if (TextUtils.isEmpty(userId)) {
      getPresenter().loadGroups();
    } else {
      getPresenter().loadSubscribedGroups(userId);
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    if (getParentController() == null) {
      ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
    }
  }

  @Override
  public void onGroupsLoaded(List<GroupRealm> groups) {
    epoxyController.setData(groups);
  }

  @Override
  public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull
  @Override
  public GroupGridOverviewPresenter createPresenter() {
    return new GroupGridOverviewPresenter(GroupRepositoryProvider.getRepository());
  }

  @Override
  public void onItemClick(View v, GroupRealm item) {
    Controller parentController = getParentController();
    if (parentController == null) {
      getRouter().pushController(RouterTransaction
          .with(GroupController.create(item.getId()))
          .pushChangeHandler(new VerticalChangeHandler())
          .popChangeHandler(new VerticalChangeHandler()));
    } else {
      parentController
          .getRouter()
          .pushController(RouterTransaction
              .with(GroupController.create(item.getId()))
              .pushChangeHandler(new VerticalChangeHandler())
              .popChangeHandler(new VerticalChangeHandler()));
    }
  }

  private void setupRecyclerView() {
    epoxyController = new GroupGridEpoxyController(this);

    boolean includeEdge = getArgs().getBoolean(KEY_INCLUDE_EDGE, true);

    rvGroupGrid.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
    rvGroupGrid.addItemDecoration(
        new GridInsetItemDecoration(spanCount, includeEdge ? DisplayUtil.dpToPx(2) : 0,
            includeEdge));
    SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvGroupGrid.setItemAnimator(animator);
    rvGroupGrid.setHasFixedSize(true);
    rvGroupGrid.setAdapter(epoxyController.getAdapter());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.label_explore_all_groups_title);
  }
}
