package com.yoloo.android.feature.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.feature.feed.globalfeed.GlobalFeedController;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.OnMaxSelectionReachedListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.feature.editor.catalog.CatalogController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import java.util.List;
import timber.log.Timber;

public class CategoryController extends MvpController<CategoryView, CategoryPresenter>
    implements CategoryView, OnItemClickListener<CategoryRealm>, OnMaxSelectionReachedListener {

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

  @Override public void onCategoriesLoaded(List<CategoryRealm> categories) {
    adapter.addCategories(categories);
  }

  @Override public void onError(Throwable throwable) {

  }

  @NonNull @Override public CategoryPresenter createPresenter() {
    return new CategoryPresenter(
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, CategoryRealm item) {
    if (multiSelection) {
      final Controller parent = getParentController();
      if (parent instanceof CatalogController) {
        ((CatalogController) parent).updateSelectedCategories(categoryType,
            adapter.getSelectedCategories());
      }
    } else {
      getParentController().getRouter()
          .pushController(RouterTransaction.with(GlobalFeedController.ofCategory(item.getName()))
              .pushChangeHandler(new VerticalChangeHandler())
              .popChangeHandler(new VerticalChangeHandler()));
    }
  }

  private void setupRecyclerView() {
    adapter = new CategoryAdapter(categoryType, this);

    final int maxSelection =
        multiSelection ? (categoryType.equals(CategoryType.TYPE_DESTINATION) ? 1 : 3) : 0;
    adapter.setMaxSelection(maxSelection);
    adapter.setOnMaxSelectionReachedListener(this);

    rvCatalog.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    rvCatalog.addItemDecoration(new GridInsetItemDecoration(2, 8, true));

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCatalog.setItemAnimator(animator);

    rvCatalog.setHasFixedSize(true);
    rvCatalog.setAdapter(adapter);
  }

  @Override public void onMaxSelectionReached() {
    Timber.d("onMaxSelectionReached()");
  }
}
