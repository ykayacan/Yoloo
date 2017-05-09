package com.yoloo.android.feature.postlist;

import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostListView extends MvpDataView<List<? super FeedItem<?>>> {

  void onAccountLoaded(AccountRealm account);

  void onPostUpdated(PostRealm post);

  void showContent();
}
