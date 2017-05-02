package com.yoloo.android.feature.feed;

import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface FeedView extends MvpDataView<List<FeedItem>> {

  void onMeLoaded(AccountRealm me);

  void showContent();
}
