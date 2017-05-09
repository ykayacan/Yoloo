package com.yoloo.android.feature.editor.editor;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface EditorView extends MvpView {

  void onMeLoaded(AccountRealm me);

  void onDraftCreated(PostRealm draft);

  void onDraftUpdated();

  void onError(Throwable t);

  void onTrendingTagsLoaded(List<TagRealm> tags);

  void onSearchTags(List<TagRealm> tags);
}
