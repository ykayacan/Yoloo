package com.yoloo.android.feature.follow;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import java.util.List;

public class FollowAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  public FollowAdapter(OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    enableDiffing();
  }

  public void addUsers(List<AccountRealm> accounts) {
    for (AccountRealm account : accounts) {
      models.add(new UserModel_()
          .account(account)
          .onProfileClickListener(onProfileClickListener)
          .onFollowClickListener(onFollowClickListener));
    }

    notifyModelsChanged();
  }

  public void clear() {
    models.clear();
    notifyModelsChanged();
  }
}
