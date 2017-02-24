package com.yoloo.android.feature.editor.compose;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpView;

interface ComposeView extends MvpView {

  void onDraftLoaded(PostRealm draft);

  void onDraftSaved(int navigation);

  void onError(Throwable t);
}
