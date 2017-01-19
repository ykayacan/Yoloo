package com.yoloo.android.feature.search;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.base.framework.MvpView;
import java.util.List;

public interface ChildSearchView extends MvpView {

  void onRecentTagsLoaded(List<TagRealm> tags);

  void onTagsLoaded(Response<List<TagRealm>> response);

  void onRecentUsersLoaded(List<AccountRealm> accounts);

  void onUsersLoaded(Response<List<AccountRealm>> response);
}
