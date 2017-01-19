package com.yoloo.android.feature.search;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.util.KeyboardUtil;

public class SearchController extends BaseController {

  @BindView(R.id.tablayout_search)
  TabLayout tabLayout;

  @BindView(R.id.viewpager_search)
  ViewPager viewPager;

  @BindView(R.id.toolbar_search)
  Toolbar toolbar;

  @BindView(R.id.ib_search_clear)
  ImageButton ibSearchClear;

  @BindView(R.id.et_search)
  EditText etSearch;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_search, container, false);
  }

  @Override
  protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    final SearchPagerAdapter pagerAdapter =
        new SearchPagerAdapter(this, true, getResources());

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);

    setupToolbar();
    setHasOptionsMenu(true);
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        KeyboardUtil.hideKeyboard(getActivity(), etSearch);
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getActivity(), etSearch);
    return super.handleBack();
  }

  @OnTextChanged(R.id.et_search)
  void listenSearch(CharSequence text) {
    ibSearchClear.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
  }

  @OnClick(R.id.ib_search_clear)
  void clearSearch() {
    etSearch.setText("");
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  private static class SearchPagerAdapter extends ControllerPagerAdapter {

    private final Resources resources;

    SearchPagerAdapter(Controller host, boolean saveControllerState, Resources resources) {
      super(host, saveControllerState);
      this.resources = resources;
    }

    @Override
    public Controller getItem(int position) {
      switch (position) {
        case 0:
          return ChildSearchController.create(SearchType.TAG);
        case 1:
          return ChildSearchController.create(SearchType.USER);
        default:
          return null;
      }
    }

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return resources.getString(R.string.label_search_tags);
        case 1:
          return resources.getString(R.string.label_search_users);
        default:
          return null;
      }
    }
  }
}
