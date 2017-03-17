package com.yoloo.android.feature.search;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface ChildSearchView extends MvpView {

  void onRecentTagsLoaded(List<TagRealm> tags);

  void onTagsLoaded(List<TagRealm> tags);

  void onRecentUsersLoaded(List<AccountRealm> accounts);

  void onUsersLoaded(List<AccountRealm> accounts);
}
