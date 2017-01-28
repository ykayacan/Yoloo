package com.yoloo.android.feature.feed.userfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

interface UserFeedView extends MvpDataView<Response<List<PostRealm>>> {

  void onAccountLoaded(AccountRealm account);

  void onTrendingCategoriesLoaded(List<CategoryRealm> topics);
}