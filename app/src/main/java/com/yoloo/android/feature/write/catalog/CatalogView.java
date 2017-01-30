package com.yoloo.android.feature.write.catalog;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpView;

public interface CatalogView extends MvpView {

  void onDraftLoaded(PostRealm draft);

  void onDraftSaved();
}
