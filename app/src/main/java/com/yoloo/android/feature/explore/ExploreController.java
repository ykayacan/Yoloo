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
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.media.MediaRepositoryProvider;
import com.yoloo.android.feature.group.GroupController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.UpdateCallback;
import java.util.List;
import timber.log.Timber;

public class ExploreController extends MvpController<ExploreView, ExplorePresenter>
    implements ExploreView, UpdateCallback<GroupRealm> {

  @BindView(R.id.recycler_view) RecyclerView rvExplore;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private ExploreAdapter adapter;

  public static ExploreController create() {
    return new ExploreController();
  }

  @NonNull
  @Override
  public ExplorePresenter createPresenter() {
    return new ExplorePresenter(MediaRepositoryProvider.getRepository(),
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
    adapter.addItems(items);
  }

  @Override
  public void onModelUpdated(GroupRealm item) {
    Timber.d("onModelUpdated(): %s", item);
    adapter.updateItem(new GroupItem(item));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    toolbar.setTitle(R.string.label_explore_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void setupRecyclerview() {
    adapter = new ExploreAdapter(getActivity(), Glide.with(getActivity()));
    adapter.setOnRecentMediaHeaderClickListener(v -> {

    });
    adapter.setOnMediaClickListener((v, model, item) -> {
      Timber.d("onMediaClick(): %s", item.getLargeSizeUrl());
    });
    adapter.setOnNewClickListener(v -> {

    });
    adapter.setOnTrendingClickListener(v -> {

    });
    adapter.setOnGroupClickListener((v, model, item) -> {
      Controller controller = GroupController.create(item.getId());
      controller.setTargetController(this);
      startTransaction(controller, new FadeChangeHandler());
    });
    adapter.setOnSubscribeClickListener((groupId, subscribed) -> {
      if (subscribed) {
        getPresenter().unsubscribe(groupId);
      } else {
        getPresenter().subscribe(groupId);
      }
    });

    rvExplore.setItemAnimator(new DefaultItemAnimator());
    rvExplore.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvExplore.setHasFixedSize(true);
    rvExplore.setAdapter(adapter);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }
}
