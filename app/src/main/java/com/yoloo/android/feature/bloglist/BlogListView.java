package com.yoloo.android.feature.bloglist;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface BlogListView extends MvpDataView<List<PostRealm>> {
  void onMeLoaded(AccountRealm me);

  void onPostUpdated(PostRealm post);
}
