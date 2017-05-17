package com.yoloo.android.feature.recentmedia;

import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

public interface RecentMediaListView extends MvpDataView<List<FeedItem<?>>> {

  void onMoreDataLoaded(List<FeedItem<?>> items);
}
