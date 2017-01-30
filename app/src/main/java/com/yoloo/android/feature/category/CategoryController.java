package com.yoloo.android.feature.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.feature.feed.postfeed.PostController;
import com.yoloo.android.feature.ui.recyclerview.GridInsetItemDecoration;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.write.catalog.CatalogController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import java.util.List;

public class CategoryController extends MvpController<CategoryView, CategoryPresenter>
    implements CategoryView, CategoryAdapter.OnCategoryClickListener {

  private static final String KEY_CATEGORY_TYPE = "CATEGORY_TYPE";
  private static final String KEY_MULTI_SELECTION = "MULTI_SELECTION";

  @BindView(R.id.rv_catalog) RecyclerView rvCatalog;

  private String categoryType;
  private boolean multiSelection;

  private CategoryAdapter adapter;

  public CategoryController(Bundle args) {
    super(args);
    categoryType = getArgs().getString(KEY_CATEGORY_TYPE);
    multiSelection = getArgs().getBoolean(KEY_MULTI_SELECTION);

    if (multiSelection) {
      setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    }
  }

  public static CategoryController create(@CategoryType String categoryType,
      boolean multiSelection) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CATEGORY_TYPE, categoryType)
        .putBoolean(KEY_MULTI_SELECTION, multiSelection)
        .build();

    return new CategoryController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_child_catalog, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupRecyclerView();
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(List<CategoryRealm> value) {
    adapter.addCategories(value);
  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public CategoryPresenter createPresenter() {
    return new CategoryPresenter(
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override
  public void onCategoryClick(View v, String categoryId, String name, boolean multiSelection) {
    if (multiSelection) {
      final Controller parent = getParentController();
      if (parent instanceof CatalogController) {
        ((CatalogController) parent).updateSelectedCategories(categoryType,
            adapter.getSelectedCategories());
      }
    } else {
      getParentController().getRouter()
          .pushController(RouterTransaction.with(PostController.ofCategory(name))
              .pushChangeHandler(new VerticalChangeHandler())
              .popChangeHandler(new VerticalChangeHandler()));
    }
  }

  private void setupRecyclerView() {
    adapter = new CategoryAdapter(categoryType);
    adapter.setMultiSelection(multiSelection);
    adapter.setMaxSelectedItems(categoryType.equals(CategoryType.TYPE_DESTINATION) ? 1 : 3);
    adapter.setOnCategoryClickListener(this);

    rvCatalog.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    rvCatalog.addItemDecoration(new GridInsetItemDecoration(2, 8, true));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCatalog.setItemAnimator(animator);

    rvCatalog.setHasFixedSize(true);
    rvCatalog.setAdapter(adapter);
  }
}