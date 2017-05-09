package com.yoloo.android.feature.search;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface ChildSearchView extends MvpView {

  void onRecentTagsLoaded(List<TagRealm> tags);

  void onTagsLoaded(List<TagRealm> tags);

  void onRecentUsersLoaded(List<AccountRealm> accounts);

  void onUsersLoaded(List<AccountRealm> accounts);
}
