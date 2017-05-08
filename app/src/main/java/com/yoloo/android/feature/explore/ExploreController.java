package com.yoloo.android.feature.explore;

import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.explore.data.ExploreItem;
import com.yoloo.android.feature.explore.data.GroupItem;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.feature.models.recentmedias.RecentMediaListModelGroup;
import com.yoloo.android.feature.postdetail.PostDetailController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.UpdateCallback;
import java.util.List;

public class ExploreController extends MvpController<ExploreView, ExplorePresenter>
    implements ExploreView, UpdateCallback<GroupRealm> {

  @BindView(R.id.recycler_view) RecyclerView rvExplore;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private ExploreEpoxyController epoxyController;

  public static ExploreController create() {
    return new ExploreController();
  }

  @NonNull
  @Override
  public ExplorePresenter createPresenter() {
    return new ExplorePresenter(PostRepositoryProvider.getRepository(),
        GroupRepositoryProvider.getRepository());
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_explore, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);
    setupToolbar();
    setupRecyclerview();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_explore, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    if (itemId == R.id.action_explore_search) {
      startTransaction(SearchController.create(), new SimpleSwapChangeHandler());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDataLoaded(List<ExploreItem<?>> items) {
    epoxyController.setData(items, null);
  }

  @Override
  public void onModelUpdated(GroupRealm item) {
    epoxyController.updateItem(new GroupItem(item));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    toolbar.setTitle(R.string.label_explore_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerview() {
    epoxyController = new ExploreEpoxyController(Glide.with(getActivity()));

    epoxyController.setRecentPhotosModelGroupCallbacks(
        new RecentMediaListModelGroup.RecentPhotosModelGroupCallbacks() {
          @Override
          public void onRecentPhotosHeaderClicked() {

          }

          @Override
          public void onRecentPhotosClicked(PostRealm post) {
            startTransaction(PostDetailController.create(post.getId()),
                new VerticalChangeHandler());
          }
        });

    epoxyController.setOnNewClickListener(
        v -> startTransaction(PostListController.ofPostSorter(PostSorter.NEWEST),
            new HorizontalChangeHandler()));
    epoxyController.setOnTrendingClickListener(
        v -> startTransaction(PostListController.ofPostSorter(PostSorter.HOT),
            new HorizontalChangeHandler()));
    epoxyController.setOnGroupClickListener((v, item) -> {
      Controller controller = GroupController.create(item.getId());
      controller.setTargetController(this);
      startTransaction(controller, new FadeChangeHandler());
    });
    epoxyController.setOnSubscribeClickListener((groupId, subscribed) -> {
      if (subscribed) {
        getPresenter().unsubscribe(groupId);
      } else {
        getPresenter().subscribe(groupId);
      }
    });

    rvExplore.setItemAnimator(new DefaultItemAnimator());
    rvExplore.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvExplore.setHasFixedSize(true);
    rvExplore.setAdapter(epoxyController.getAdapter());
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}
