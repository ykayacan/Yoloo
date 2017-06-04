package com.yoloo.android.feature.explore;

import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpView;
import java.util.Collection;

interface ExploreView extends MvpView {

  void onDataLoaded(Collection<FeedItem<?>> items);
}
