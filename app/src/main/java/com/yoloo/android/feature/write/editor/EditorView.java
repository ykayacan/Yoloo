package com.yoloo.android.feature.write.editor;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.base.framework.MvpView;
import java.util.List;

interface EditorView extends MvpView {

  void onRecommendedTagsLoaded(List<TagRealm> tags);

  void onSuggestedTagsLoaded(List<TagRealm> tags);

  void onError(Throwable t);
}
