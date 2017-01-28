package com.yoloo.android.feature.write.bountyoverview;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpView;

public interface BountyView extends MvpView {

  void onDraftAndAccountLoaded(PostRealm draft, AccountRealm account);

  void onDraftSaved();

  void onError(Throwable t);
}
