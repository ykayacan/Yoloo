package com.yoloo.android.feature.editor.editor;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface EditorQuestionView extends MvpView {

  void onDraftCreated(PostRealm draft);

  void onDraftUpdated(int navigation);

  void onError(Throwable t);

  void onRecommendedTagsLoaded(List<TagRealm> tags);

  void onSearchTags(List<TagRealm> tags);
}
