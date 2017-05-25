package com.yoloo.android.feature.postlist;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostListView extends MvpDataView<List<FeedItem<?>>> {

  void onPostUpdated(@NonNull PostRealm post);

  void showContent();

  void onMoreLoaded(List<FeedItem<?>> items);
}
