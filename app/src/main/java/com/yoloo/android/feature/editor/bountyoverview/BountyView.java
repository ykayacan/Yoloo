package com.yoloo.android.feature.editor.bountyoverview;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpView;

public interface BountyView extends MvpView {

  void onDraftAndAccountLoaded(PostRealm draft, AccountRealm account);

  void onDraftSaved();

  void onError(Throwable t);
}
