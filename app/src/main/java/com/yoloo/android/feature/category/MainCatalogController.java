package com.yoloo.android.feature.category;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;

public class MainCatalogController extends BaseController {

  @BindView(R.id.tablayout_catalog) TabLayout tabLayout;

  @BindView(R.id.viewpager_catalog) ViewPager viewPager;

  @BindView(R.id.toolbar_catalog) Toolbar toolbar;

  public MainCatalogController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_catalog, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
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

  @Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.clear();
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_main_catalog_toolbar_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
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
          return CategoryController.create(CategoryType.TYPE_DESTINATION, false);
        case 1:
          return CategoryController.create(CategoryType.TYPE_THEME, false);
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
          return resources.getString(R.string.label_catalog_tab_destination);
        case 1:
          return resources.getString(R.string.label_catalog_tab_theme);
        default:
          return null;
      }
    }
  }
}