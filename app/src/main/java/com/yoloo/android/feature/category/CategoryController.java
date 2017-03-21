package com.yoloo.android.feature.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.OnMaxSelectionReachedListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import timber.log.Timber;

public class CategoryController extends MvpController<CategoryView, CategoryPresenter>
    implements CategoryView, OnItemClickListener<CategoryRealm>, OnMaxSelectionReachedListener {

  private static final String KEY_MAX_SELECTION = "MAX_SELECTION";
  private static final String KEY_SPAN_COUNT = "SPAN_COUNT";
  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.rv_catalog) RecyclerView rvCatalog;

  private int maxSelection = 0;
  private int spanCount;

  private CategoryAdapter adapter;

  private SelectedCategoriesListener selectedCategoriesListener;
  private OnCategoryClickListener onCategoryClickListener;

  public CategoryController(Bundle args) {
    super(args);
    maxSelection = getArgs().getInt(KEY_MAX_SELECTION);
    spanCount = getArgs().getInt(KEY_SPAN_COUNT, 2);

    if (isMultiSelection()) {
      setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    }
  }

  public static CategoryController create() {
    return create(0);
  }

  public static CategoryController create(int maxSelection) {
    final Bundle bundle = new BundleBuilder().putInt(KEY_MAX_SELECTION, maxSelection).build();

    return new CategoryController(bundle);
  }

  public static CategoryController create(@Nonnull String userId, int spanCount) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .putInt(KEY_SPAN_COUNT, spanCount)
        .build();

    return new CategoryController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_child_catalog, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    final String userId = getArgs().getString(KEY_USER_ID, null);

    if (TextUtils.isEmpty(userId)) {
      getPresenter().loadCategories();
    } else {
      getPresenter().loadInterestedCategories(userId);
    }
  }

  @Override public void onCategoriesLoaded(List<CategoryRealm> categories) {
    adapter.addCategories(categories);
  }

  @Override public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull @Override public CategoryPresenter createPresenter() {
    return new CategoryPresenter(
        CategoryRepository.getInstance(
            CategoryRemoteDataStore.getInstance(),
            CategoryDiskDataStore.getInstance()));
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, CategoryRealm item) {
    if (isMultiSelection()) {
      if (selectedCategoriesListener != null) {
        selectedCategoriesListener.selectedCategories(adapter.getSelectedCategories());
      }
    } else {
      if (onCategoryClickListener != null) {
        onCategoryClickListener.onCategoryClick(v, item);
      }
    }
  }

  @Override public void onMaxSelectionReached() {
    Timber.d("onMaxSelectionReached()");
  }

  private void setupRecyclerView() {
    adapter = new CategoryAdapter(this);

    adapter.setMaxSelection(maxSelection);
    adapter.setOnMaxSelectionReachedListener(this);

    rvCatalog.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
    rvCatalog.addItemDecoration(
        new GridInsetItemDecoration(spanCount, DisplayUtil.dpToPx(2), true));
    SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCatalog.setItemAnimator(animator);
    rvCatalog.setHasFixedSize(true);
    rvCatalog.setAdapter(adapter);
  }

  private boolean isMultiSelection() {
    return maxSelection != 0;
  }

  public void setSelectedCategoriesListener(SelectedCategoriesListener selectedCategoriesListener) {
    this.selectedCategoriesListener = selectedCategoriesListener;
  }

  public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener) {
    this.onCategoryClickListener = onCategoryClickListener;
  }

  public interface SelectedCategoriesListener {
    void selectedCategories(List<CategoryRealm> selected);
  }

  public interface OnCategoryClickListener {
    void onCategoryClick(View v, CategoryRealm category);
  }
}
