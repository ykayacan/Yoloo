package com.yoloo.android.feature.postlist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostListView extends MvpDataView<List<PostRealm>> {

  void onAccountLoaded(AccountRealm account);

  void onPostUpdated(PostRealm post);

  void showContent();
}
