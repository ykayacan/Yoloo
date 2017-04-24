package com.yoloo.android.feature.groupgridoverview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

public class GroupGridOverviewController
    extends MvpController<GroupGridOverviewView, GroupGridOverviewPresenter>
    implements GroupGridOverviewView, OnItemClickListener<GroupRealm> {

  private static final String KEY_SPAN_COUNT = "SPAN_COUNT";
  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.rv_catalog) RecyclerView rvCatalog;

  private int spanCount;

  private GroupGridAdapter adapter;

  public GroupGridOverviewController() {
  }

  public GroupGridOverviewController(Bundle args) {
    super(args);
    spanCount = getArgs().getInt(KEY_SPAN_COUNT, 3);
  }

  public static GroupGridOverviewController create() {
    return new GroupGridOverviewController();
  }

  public static GroupGridOverviewController create(@Nonnull String userId, int spanCount) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putInt(KEY_SPAN_COUNT, spanCount)
        .build();

    return new GroupGridOverviewController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_child_catalog, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
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
  public void onGroupsLoaded(List<GroupRealm> categories) {
    adapter.addCategories(categories);
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
  public void onItemClick(View v, EpoxyModel<?> model, GroupRealm item) {
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
    adapter = new GroupGridAdapter(this);

    rvCatalog.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
    rvCatalog.addItemDecoration(
        new GridInsetItemDecoration(spanCount, DisplayUtil.dpToPx(2), true));
    SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCatalog.setItemAnimator(animator);
    rvCatalog.setHasFixedSize(true);
    rvCatalog.setAdapter(adapter);
  }
}
