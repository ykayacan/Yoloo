package com.yoloo.android.feature.search;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.util.KeyboardUtil;

public class SearchController extends BaseController {

  @BindView(R.id.tablayout_search) TabLayout tabLayout;
  @BindView(R.id.viewpager_search) ViewPager viewPager;
  @BindView(R.id.toolbar_search) Toolbar toolbar;
  @BindView(R.id.ib_search_clear) ImageButton ibSearchClear;
  @BindView(R.id.et_search) EditText etSearch;

  public SearchController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static SearchController create() {
    return new SearchController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_search, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    final RouterPagerAdapter pagerAdapter =
        new SearchPagerAdapter(this, getResources());

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);

    setupToolbar();
    setHasOptionsMenu(true);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(etSearch);
    return super.handleBack();
  }

  @OnTextChanged(R.id.et_search) void listenSearch(CharSequence text) {
    ibSearchClear.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
  }

  @OnClick(R.id.ib_search_clear) void clearSearch() {
    etSearch.setText("");
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private static class SearchPagerAdapter extends RouterPagerAdapter {

    private final Resources resources;

    SearchPagerAdapter(@NonNull Controller host, Resources resources) {
      super(host);
      this.resources = resources;
    }

    @Override public int getCount() {
      return 2;
    }

    @Override public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return resources.getString(R.string.label_search_tags);
        case 1:
          return resources.getString(R.string.label_search_users);
        default:
          return null;
      }
    }

    @Override public void configureRouter(@NonNull Router router, int position) {
      @SearchType final int searchType = position == 0 ? SearchType.TAG : SearchType.USER;

      if (!router.hasRootController()) {
        ChildSearchController page = ChildSearchController.create(searchType);
        router.setRoot(RouterTransaction.with(page));
      }
    }
  }
}
