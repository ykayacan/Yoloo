package com.yoloo.android.feature.feed;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface FeedView extends MvpDataView<List<FeedItem<?>>> {

  void onMeLoaded(@NonNull AccountRealm me);

  void onPostUpdated(@NonNull PostRealm post);

  void onMoreLoaded(List<FeedItem<?>> items);

  void showContent();
}
