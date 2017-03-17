package com.yoloo.android.feature.editor.editorcategorylist;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpView;

public interface EditorCategoryListView extends MvpView {

  void onDraftLoaded(PostRealm draft);

  void onDraftSaved();
}
