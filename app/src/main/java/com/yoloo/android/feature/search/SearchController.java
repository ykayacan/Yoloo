package com.yoloo.android.feature.search;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import butterknife.BindString;
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
import com.yoloo.android.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class SearchController extends BaseController {

  @BindView(R.id.tablayout_search) TabLayout tabLayout;
  @BindView(R.id.viewpager_search) ViewPager viewPager;
  @BindView(R.id.toolbar_search) Toolbar toolbar;
  @BindView(R.id.ib_search_clear) ImageButton ibSearchClear;
  @BindView(R.id.et_search) EditText etSearch;

  @BindString(R.string.label_search_tags) String searchTagsString;
  @BindString(R.string.label_search_users) String searchUsersString;

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

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    List<Pair<String, Controller>> pairs = new ArrayList<>(2);
    pairs.add(Pair.create(searchTagsString, ChildSearchController.create(SearchType.TAG)));
    pairs.add(Pair.create(searchUsersString, ChildSearchController.create(SearchType.USER)));

    final RouterPagerAdapter pagerAdapter = new SearchPagerAdapter(this, pairs);

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
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(etSearch);
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

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private static class SearchPagerAdapter extends RouterPagerAdapter {
    private final List<Pair<String, Controller>> pairs;

    SearchPagerAdapter(@NonNull Controller host, List<Pair<String, Controller>> pairs) {
      super(host);
      this.pairs = pairs;
    }

    @Override
    public int getCount() {
      return pairs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return pairs.get(position).first;
    }

    @Override
    public void configureRouter(@NonNull Router router, int position) {
      if (!router.hasRootController()) {
        router.setRoot(RouterTransaction.with(pairs.get(position).second));
      }
    }
  }
}
