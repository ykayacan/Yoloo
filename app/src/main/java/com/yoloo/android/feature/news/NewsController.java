package com.yoloo.android.feature.news;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SpannedGridLayoutManager;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.ItemDecorationAlbumColumns;
import com.yoloo.android.ui.widget.StateLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class NewsController extends MvpController<NewsView, NewsPresenter> implements NewsView,
    OnItemClickListener<NewsRealm> {

  @BindView(R.id.root_view) StateLayout rootView;
  @BindView(R.id.rv_news) RecyclerView rvNews;
  @BindView(R.id.toolbar_news) Toolbar toolbar;

  private NewsAdapter adapter;

  public static NewsController create() {
    return new NewsController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_news, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();

    NewsRealm n1 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Interrailde Büyük Gelişme")
        .setCover(true)
        .setBgImageUrl("https://www.uzakrota.com/wp-content/uploads/2017/02/"
            + "edinburgh-730x548.jpg");

    NewsRealm n2 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Vizeler Kalktı")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/mercator-"
                + "Accelya-warburg-pincus-airline-services-730x548.jpg");

    NewsRealm n3 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Gezginler Evi Burada Açılıyor")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/Airbnb-helps-combat-"
                + "winter-blues-with-magical-green-wonderland-in-London-730x492.jpg");

    NewsRealm n4 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Yeni Yerler Keşfetmenin Tam Zamanı")
        .setBgImageUrl(
            "http://webneel.com/daily/sites/default/files/images/daily/10-2013/1-"
                + "travel-photography.preview.jpg");

    NewsRealm n5 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Interrailde Büyük Gelişme")
        .setCover(true)
        .setBgImageUrl("https://www.uzakrota.com/wp-content/uploads/2017/02/"
            + "edinburgh-730x548.jpg");

    NewsRealm n6 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Vizeler Kalktı")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/mercator-"
                + "Accelya-warburg-pincus-airline-services-730x548.jpg");

    NewsRealm n7 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Gezginler Evi Burada Açılıyor")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/Airbnb-helps-combat-"
                + "winter-blues-with-magical-green-wonderland-in-London-730x492.jpg");

    NewsRealm n8 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Yeni Yerler Keşfetmenin Tam Zamanı")
        .setBgImageUrl(
            "http://webneel.com/daily/sites/default/files/images/daily/10-2013/1-"
                + "travel-photography.preview.jpg");

    List<NewsRealm> list = new ArrayList<>();
    list.add(n1);
    list.add(n2);
    list.add(n3);
    list.add(n4);
    list.add(n5);
    list.add(n6);
    list.add(n7);
    list.add(n8);

    onLoaded(list);

    rootView.setContentView(rvNews);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onLoading(boolean pullToRefresh) {
    rootView.setState(StateLayout.VIEW_STATE_LOADING);
  }

  @Override public void onLoaded(List<NewsRealm> value) {
    adapter.addNews(value);

    rootView.setState(StateLayout.VIEW_STATE_CONTENT);
  }

  @Override public void onError(Throwable e) {
    rootView.setState(StateLayout.VIEW_STATE_ERROR);
    Timber.e(e);
  }

  @Override public void onEmpty() {
    rootView.setState(StateLayout.VIEW_STATE_EMPTY);
  }

  @NonNull @Override public NewsPresenter createPresenter() {
    return new NewsPresenter();
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, NewsRealm item) {

  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(getResources().getString(R.string.label_news_toolbar_title));
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupRecyclerView() {
    adapter = new NewsAdapter(this);

    SpannedGridLayoutManager lm = new SpannedGridLayoutManager(position -> {
      if (position == 0) {
        return new SpannedGridLayoutManager.SpanInfo(2, 2);
      } else {
        return new SpannedGridLayoutManager.SpanInfo(1, 1);
      }
    }, 3, 1f);

    rvNews.setLayoutManager(lm);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvNews.setItemAnimator(animator);

    rvNews.addItemDecoration(new ItemDecorationAlbumColumns(2, 2));

    rvNews.setHasFixedSize(true);
    rvNews.setAdapter(adapter);
  }
}
