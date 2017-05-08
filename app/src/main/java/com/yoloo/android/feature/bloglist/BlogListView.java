package com.yoloo.android.feature.bloglist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface BlogListView extends MvpDataView<List<PostRealm>> {
  void onMeLoaded(AccountRealm me);

  void onPostUpdated(PostRealm post);
}
