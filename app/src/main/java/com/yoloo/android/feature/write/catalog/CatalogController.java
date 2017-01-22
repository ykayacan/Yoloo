package com.yoloo.android.feature.write.catalog;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.category.CategoryController;
import com.yoloo.android.feature.category.CategoryType;
import com.yoloo.android.feature.write.editor.EditorController;
import io.reactivex.Observable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

public class CatalogController extends MvpController<CatalogView, CatalogPresenter>
    implements CatalogView {

  @BindView(R.id.tablayout_catalog) TabLayout tabLayout;

  @BindView(R.id.viewpager_catalog) ViewPager viewPager;

  @BindView(R.id.toolbar_catalog) Toolbar toolbar;

  private PostRealm draft;

  private Map<String, List<CategoryRealm>> selectedCategories;

  public CatalogController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_catalog, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    selectedCategories = new HashMap<>();

    final ControllerPagerAdapter pagerAdapter = new CatalogPagerAdapter(this, true, getResources());

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);

    setupToolbar();
    setHasOptionsMenu(true);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_catalog, menu);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        showDiscardDraftDialog();
        return false;
      case R.id.action_next:
        if (selectedCategories.isEmpty()) {
          Snackbar.make(getView(), R.string.error_select_category, Snackbar.LENGTH_SHORT).show();
          return false;
        }

        String categoryIdsAsString = getCategoryIdsAsString();

        draft.setCategoriesAsString(categoryIdsAsString);

        getPresenter().updateDraft(draft);

        getRouter().pushController(RouterTransaction.with(new EditorController())
            .pushChangeHandler(new HorizontalChangeHandler())
            .popChangeHandler(new HorizontalChangeHandler()));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @NonNull @Override public CatalogPresenter createPresenter() {
    return new CatalogPresenter(PostRepository.getInstance(PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance()));
  }

  @Override public void onDraftLoaded(PostRealm draft) {
    this.draft = draft;
    Timber.d("Catalog: %s", draft.getBounty());
  }

  private String getCategoryIdsAsString() {
    return Observable.fromIterable(selectedCategories.values())
        .flatMap(Observable::fromIterable)
        .map(CategoryRealm::getId)
        .reduce((s, s2) -> s + "," + s2)
        .map(s -> s.substring(0, s.length() - 1))
        .blockingGet();
  }

  public void updateSelectedCategories(@CategoryType String categoryType,
      List<CategoryRealm> categories) {
    if (categories.size() == 0) {
      selectedCategories.remove(categoryType);
    } else if (categoryType.equals(CategoryType.TYPE_DESTINATION)) {
      selectedCategories.put(CategoryType.TYPE_DESTINATION, categories);
    } else if (categoryType.equals(CategoryType.TYPE_THEME)) {
      selectedCategories.put(CategoryType.TYPE_THEME, categories);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_catalog_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeAsUpIndicator(
          AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void showDiscardDraftDialog() {
    new AlertDialog.Builder(getActivity()).setTitle(R.string.label_discard_draft)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          getPresenter().deleteDraft();
          getRouter().popCurrentController();
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        })
        .show();
  }

  private static class CatalogPagerAdapter extends ControllerPagerAdapter {

    private final Resources resources;

    CatalogPagerAdapter(Controller host, boolean saveControllerState, Resources resources) {
      super(host, saveControllerState);
      this.resources = resources;
    }

    @Override public Controller getItem(int position) {
      switch (position) {
        case 0:
          return CategoryController.create(CategoryType.TYPE_DESTINATION, true);
        case 1:
          return CategoryController.create(CategoryType.TYPE_THEME, true);
        default:
          return null;
      }
    }

    @Override public int getCount() {
      return 2;
    }

    @Override public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return resources.getString(R.string.label_destination);
        case 1:
          return resources.getString(R.string.label_theme);
        default:
          return null;
      }
    }
  }
}
