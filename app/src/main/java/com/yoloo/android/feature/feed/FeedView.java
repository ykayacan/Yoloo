package com.yoloo.android.feature.feed;

import android.content.Context;
import android.support.annotation.NonNull;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.framework.MvpDataView;

interface FeedView extends MvpDataView<FeedPresenter.FeedState> {

  Context getAppContext();

  void onMeLoaded(@NonNull AccountRealm me);
}
