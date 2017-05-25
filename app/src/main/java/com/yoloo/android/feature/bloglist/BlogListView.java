package com.yoloo.android.feature.bloglist;

import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface BlogListView extends MvpDataView<List<FeedItem<?>>> {

  void onPostUpdated(PostRealm post);
}
