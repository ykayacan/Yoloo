package com.yoloo.android.feature.search.tag;

import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface ChildTagSearchView extends MvpView {

  void onRecentTagsLoaded(List<TagRealm> tags);

  void onTagsLoaded(List<TagRealm> tags);
}
