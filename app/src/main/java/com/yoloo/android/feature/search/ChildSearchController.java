package com.yoloo.android.feature.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyControllerAdapter;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.ProfileController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChildSearchController extends MvpController<ChildSearchView, ChildSearchPresenter>
    implements ChildSearchView, OnItemClickListener<TagRealm>, OnProfileClickListener,
    OnFollowClickListener {

  private static final String KEY_SEARCH_TYPE = "SEARCH_TYPE";

  private final PublishSubject<String> searchSubject = PublishSubject.create();

  @BindView(R.id.rv_child_search) RecyclerView rvChildSearch;

  private SearchUserEpoxyController userEpoxyController;
  private SearchTagEpoxyController tagEpoxyController;

  private int searchType;

  private EditText etSearch;
  private ViewPager viewPager;

  private ViewPager.OnPageChangeListener onPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
          etSearch.setText("");
        }
      };

  public ChildSearchController(@Nullable Bundle args) {
    super(args);
  }

  public static ChildSearchController create(@SearchType int type) {
    final Bundle bundle = new BundleBuilder().putInt(KEY_SEARCH_TYPE, type).build();

    return new ChildSearchController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_child_search, container, false);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    searchType = getArgs().getInt(KEY_SEARCH_TYPE);

    etSearch = ButterKnife.findById(getParentController().getView(), R.id.et_search);

    viewPager = ButterKnife.findById(getParentController().getView(), R.id.viewpager_search);
    viewPager.addOnPageChangeListener(onPageChangeListener);

    if (searchType == SearchType.TAG) {
      tagEpoxyController = new SearchTagEpoxyController(this);
      setupRecyclerView(tagEpoxyController.getAdapter());
      getPresenter().loadRecentTags();
    } else if (searchType == SearchType.USER) {
      userEpoxyController = new SearchUserEpoxyController(getActivity(), this, this);
      setupRecyclerView(userEpoxyController.getAdapter());
      getPresenter().loadRecentUsers();
    }

    RxTextView.afterTextChangeEvents(etSearch)
        .map(event -> event.editable().toString())
        .filter(s -> !s.isEmpty())
        .debounce(400, TimeUnit.MILLISECONDS)
        .subscribe(query -> loadBySearchType(query, true));
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    viewPager.removeOnPageChangeListener(onPageChangeListener);
  }

  @NonNull
  @Override
  public ChildSearchPresenter createPresenter() {
    return new ChildSearchPresenter(TagRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onRecentTagsLoaded(List<TagRealm> tags) {
    tagEpoxyController.setData(tags);
  }

  @Override
  public void onTagsLoaded(List<TagRealm> tags) {
    tagEpoxyController.setData(tags);
  }

  @Override
  public void onRecentUsersLoaded(List<AccountRealm> accounts) {
    userEpoxyController.setData(accounts);
  }

  @Override
  public void onUsersLoaded(List<AccountRealm> accounts) {
    userEpoxyController.setData(accounts);
  }

  @Override
  public void onItemClick(View v, TagRealm item) {
    KeyboardUtil.hideKeyboard(etSearch);

    getParentController()
        .getRouter()
        .pushController(RouterTransaction
            .with(PostListController.ofTag(item.getName()))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override
  public void onProfileClick(View v, String userId) {
    KeyboardUtil.hideKeyboard(etSearch);

    getParentController()
        .getRouter()
        .pushController(RouterTransaction
            .with(ProfileController.create(userId))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override
  public void onFollowClick(View v, AccountRealm account, int direction) {
    getPresenter().follow(account.getId(), direction);
  }

  private void setupRecyclerView(EpoxyControllerAdapter adapter) {
    rvChildSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvChildSearch.setItemAnimator(new DefaultItemAnimator());
    rvChildSearch.setHasFixedSize(true);
    rvChildSearch.setAdapter(adapter);
  }

  private void loadBySearchType(String query, boolean resetCursor) {
    if (searchType == SearchType.TAG) {
      getPresenter().searchTags(query, resetCursor);
    } else if (searchType == SearchType.USER) {
      getPresenter().searchUsers(query, resetCursor);
    }
  }
}
