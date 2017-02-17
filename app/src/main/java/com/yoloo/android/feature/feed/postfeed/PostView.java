package com.yoloo.android.feature.feed.postfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostView extends MvpDataView<Response<List<PostRealm>>> {

  void onAccountLoaded(AccountRealm account);

  void onPostUpdated(PostRealm post);

  void showContent();
}
