package com.yoloo.android.feature.search.tag;

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
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepositoryProvider;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.KeyboardUtil;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChildTagSearchController extends MvpController<ChildTagSearchView, ChildTagSearchPresenter>
    implements ChildTagSearchView, OnItemClickListener<TagRealm> {

  @BindView(R.id.rv_child_search) RecyclerView recyclerView;

  private SearchTagEpoxyController epoxyController;

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

  public static ChildTagSearchController create() {
    return new ChildTagSearchController();
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

      getPresenter().loadRecentTags();

      RxTextView.afterTextChangeEvents(etSearch)
          .map(event -> event.editable().toString())
          .filter(s -> !s.isEmpty())
          .debounce(400, TimeUnit.MILLISECONDS)
          .subscribe(query -> getPresenter().searchTags(query, true));

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
  public ChildTagSearchPresenter createPresenter() {
    return new ChildTagSearchPresenter(TagRepositoryProvider.getRepository()
    );
  }

  @Override
  public void onRecentTagsLoaded(List<TagRealm> tags) {
    epoxyController.setData(tags);
  }

  @Override
  public void onTagsLoaded(List<TagRealm> tags) {
    epoxyController.setData(tags);
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

  private void setupRecyclerView() {
    epoxyController = new SearchTagEpoxyController(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setItemAnimator(new SlideInItemAnimator());
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(epoxyController.getAdapter());
  }
}
