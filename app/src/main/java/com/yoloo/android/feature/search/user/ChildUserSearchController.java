package com.yoloo.android.feature.search.user;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.Observable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class ChildUserSearchController
    extends MvpController<ChildUserSearchView, ChildUserSearchPresenter>
    implements ChildUserSearchView, OnProfileClickListener {

  @BindView(R.id.rv_child_search) RecyclerView recyclerView;

  private SearchUserEpoxyController epoxyController;

  private EditText etSearch;
  private ViewPager viewPager;

  private boolean reEnter;

  private ViewPager.OnPageChangeListener onPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
          etSearch.setText("");
        }
      };

  public static ChildUserSearchController create() {
    return new ChildUserSearchController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_child_search, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupRecyclerView();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    etSearch = ButterKnife.findById(getParentController().getView(), R.id.et_search);

    if (!reEnter) {
      viewPager = ButterKnife.findById(getParentController().getView(), R.id.viewpager_search);
      viewPager.addOnPageChangeListener(onPageChangeListener);

      getPresenter().loadRecentUsers();

      RxTextView.afterTextChangeEvents(etSearch)
          .map(event -> event.editable().toString())
          .filter(query -> !query.isEmpty())
          .debounce(400, TimeUnit.MILLISECONDS)
          .switchMap(query -> {
            getPresenter().searchUsers(query, true);
            return Observable.just(query);
          })
          .subscribe(s -> {
          }, Timber::e);

      reEnter = true;
    }
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    viewPager.removeOnPageChangeListener(onPageChangeListener);
  }

  @NonNull
  @Override
  public ChildUserSearchPresenter createPresenter() {
    return new ChildUserSearchPresenter(
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onRecentUsersLoaded(List<AccountRealm> accounts) {
    epoxyController.setData(accounts);
  }

  @Override
  public void onUsersLoaded(List<AccountRealm> accounts) {
    epoxyController.setData(accounts);
  }

  @Override
  public void onProfileClick(View v, String userId) {
    KeyboardUtil.hideKeyboard(etSearch);

    getParentController().getRouter().pushController(RouterTransaction
        .with(ProfileController.create(userId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupRecyclerView() {
    epoxyController = new SearchUserEpoxyController(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setItemAnimator(new SlideInItemAnimator());
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(epoxyController.getAdapter());
  }
}
