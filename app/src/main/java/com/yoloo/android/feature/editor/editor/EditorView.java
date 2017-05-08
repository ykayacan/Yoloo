package com.yoloo.android.feature.editor.editor;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
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
