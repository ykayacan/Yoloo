package com.yoloo.android.feature.editor.selectbounty;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.framework.MvpView;

public interface BountySelectView extends MvpView {

  void onDraftAndAccountLoaded(PostRealm draft, AccountRealm account);

  void onDraftSaved();

  void onError(Throwable t);
}
