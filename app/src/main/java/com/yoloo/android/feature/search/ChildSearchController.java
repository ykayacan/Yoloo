package com.yoloo.android.feature.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.globalfeed.GlobalFeedController;
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

  private final TextWatcher watcher = new TextWatcher() {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      searchSubject.onNext(s.toString());
    }

    @Override public void afterTextChanged(Editable s) {

    }
  };

  @BindView(R.id.rv_child_search) RecyclerView rvChildSearch;
  private SearchAdapter adapter;

  private int searchType;

  private String tagCursor;
  private String userCursor;

  private EditText etSearch;
  private ViewPager viewPager;

  private ViewPager.OnPageChangeListener onPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override public void onPageSelected(int position) {
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

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    searchType = getArgs().getInt(KEY_SEARCH_TYPE);

    etSearch = ButterKnife.findById(getParentController().getView(), R.id.et_search);

    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    viewPager = ButterKnife.findById(getParentController().getView(), R.id.viewpager_search);

    viewPager.addOnPageChangeListener(onPageChangeListener);

    if (searchType == SearchType.TAG) {
      getPresenter().loadRecentTags();
    } else if (searchType == SearchType.USER) {
      getPresenter().loadRecentUsers();
    }

    etSearch.addTextChangedListener(watcher);

    searchSubject.filter(s -> !s.isEmpty()).debounce(350, TimeUnit.MILLISECONDS)
        .subscribe(this::loadBySearchType);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    etSearch.removeTextChangedListener(watcher);
    viewPager.removeOnPageChangeListener(onPageChangeListener);
  }

  @NonNull @Override public ChildSearchPresenter createPresenter() {
    return new ChildSearchPresenter(
        TagRepository.getInstance(TagRemoteDataStore.getInstance(), TagDiskDataStore.getInstance()),
        UserRepository.getInstance(UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onRecentTagsLoaded(List<TagRealm> tags) {
    adapter.replaceTags(tags);
  }

  @Override public void onTagsLoaded(Response<List<TagRealm>> response) {
    tagCursor = response.getCursor();

    adapter.replaceTags(response.getData());
  }

  @Override public void onRecentUsersLoaded(List<AccountRealm> accounts) {
    adapter.replaceUsers(accounts);
  }

  @Override public void onUsersLoaded(Response<List<AccountRealm>> response) {
    userCursor = response.getCursor();

    adapter.replaceUsers(response.getData());
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, TagRealm item) {
    KeyboardUtil.hideKeyboard(etSearch);

    getParentController().getRouter()
        .pushController(RouterTransaction.with(GlobalFeedController.ofTag(item.getName()))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onProfileClick(View v, String ownerId) {
    KeyboardUtil.hideKeyboard(etSearch);

    getParentController().getRouter()
        .pushController(RouterTransaction.with(ProfileController.create(ownerId))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onFollowClick(View v, String userId, int direction) {
    getPresenter().follow(userId, direction);
  }

  private void setupRecyclerView() {
    adapter = new SearchAdapter(getActivity(), this, this, this);

    rvChildSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvChildSearch.setItemAnimator(new DefaultItemAnimator());
    rvChildSearch.setHasFixedSize(true);
    rvChildSearch.setAdapter(adapter);
  }

  private void loadBySearchType(String s) {
    if (searchType == SearchType.TAG) {
      getPresenter().loadTags(s, tagCursor);
    } else if (searchType == SearchType.USER) {
      getPresenter().loadUsers(s, userCursor);
    }
  }
}
