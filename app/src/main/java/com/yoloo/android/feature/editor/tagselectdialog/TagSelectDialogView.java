package com.yoloo.android.feature.editor.tagselectdialog;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface TagSelectDialogView extends MvpView {

  void onRecommendedTagsLoaded(List<TagRealm> tags);

  void onSearchTags(List<TagRealm> tags);
}
