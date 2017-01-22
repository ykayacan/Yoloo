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
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.globalfeed.GlobalFeedController;
import com.yoloo.android.util.BundleBuilder;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChildSearchController extends MvpController<ChildSearchView, ChildSearchPresenter>
    implements ChildSearchView, OnTagClickListener, OnProfileClickListener {

  private static final String KEY_SEARCH_TYPE = "SEARCH_TYPE";

  @BindView(R.id.rv_child_search) RecyclerView rvChildSearch;

  private PublishSubject<String> SEARCH_SUBJECT = PublishSubject.create();

  private TextWatcher watcher = new TextWatcher() {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      SEARCH_SUBJECT.onNext(s.toString());
    }

    @Override public void afterTextChanged(Editable s) {

    }
  };

  private SearchAdapter adapter;

  private int searchType;

  private String tagCursor;
  private String userCursor;

  private EditText etSearch;
  private ViewPager viewPager;

  private ViewPager.OnPageChangeListener onPageChangeListener =
      new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override public void onPageSelected(int position) {
          etSearch.setText("");
        }

        @Override public void onPageScrollStateChanged(int state) {

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

    SEARCH_SUBJECT.filter(s -> !s.isEmpty()).debounce(350, TimeUnit.MILLISECONDS).subscribe(s -> {
      if (searchType == SearchType.TAG) {
        getPresenter().loadTags(s, tagCursor);
      } else if (searchType == SearchType.USER) {
        getPresenter().loadUsers(s, userCursor);
      }
    });
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

  @Override public void onTagClick(String name) {
    getParentController().getRouter()
        .pushController(RouterTransaction.with(GlobalFeedController.createFromTag(name))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  @Override public void onProfileClick(View v, String ownerId) {

  }

  private void setupRecyclerView() {
    adapter = new SearchAdapter(this, this);

    rvChildSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvChildSearch.setItemAnimator(new DefaultItemAnimator());
    rvChildSearch.setHasFixedSize(true);
    rvChildSearch.setAdapter(adapter);
  }
}
