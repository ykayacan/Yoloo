package com.yoloo.android.feature.write.bounty;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpView;

public interface BountyView extends MvpView {

  void onDraftLoaded(PostRealm draft);

  void onTotalBounty(int bounty);

  void onBountyRenewed(int bounty);

  void onBountyConsumed(int bounties);

  void onError(Throwable t);
}
