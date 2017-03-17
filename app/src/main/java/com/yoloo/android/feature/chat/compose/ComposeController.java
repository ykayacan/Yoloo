package com.yoloo.android.feature.chat.compose;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.google.firebase.database.FirebaseDatabase;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import java.util.List;

public class ComposeController
    extends MvpController<ComposeView, ComposePresenter>
    implements ComposeView, OnProfileClickListener,
    EndlessRecyclerViewScrollListener.OnLoadMoreListener {

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.searchview_createconversation) SearchView searchView;
  @BindView(R.id.search_edit_frame) Toolbar toolbar;
  @BindView(R.id.rv_createconversation) RecyclerView rvCreateConversation;

  private ComposeAdapter adapter;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public ComposeController(@Nullable Bundle args) {
    super(args);
  }

  public static ComposeController create(String userId) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .build();

    return new ComposeController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_createconversation, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();

    SearchManager searchManager =
        (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    rvCreateConversation.addOnScrollListener(endlessRecyclerViewScrollListener);

    getPresenter().loadFollowers(getArgs().getString(KEY_USER_ID));
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    rvCreateConversation.removeOnScrollListener(endlessRecyclerViewScrollListener);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(Response<List<AccountRealm>> value) {
    adapter.addContacts(value.getData());
  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {

  }

  @Override public void onLoadMore() {

  }

  @NonNull @Override public ComposePresenter createPresenter() {
    return new ComposePresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        FirebaseDatabase.getInstance().getReference());
  }

  @Override public void onProfileClick(View v, EpoxyModel<?> model, String userId) {
    getPresenter().createConversation("a1", AccountFaker.generateOne());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setDisplayShowHomeEnabled(true);
  }

  private void setupRecyclerView() {
    adapter = new ComposeAdapter(getActivity(), this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvCreateConversation.setLayoutManager(lm);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCreateConversation.setItemAnimator(animator);

    rvCreateConversation.setHasFixedSize(true);
    rvCreateConversation.setAdapter(adapter);

    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(lm, this);
  }
}
