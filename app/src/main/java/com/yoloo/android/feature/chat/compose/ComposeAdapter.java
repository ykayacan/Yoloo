package com.yoloo.android.feature.chat.compose;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import java.util.List;

public class ComposeAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;

  public ComposeAdapter(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;

    enableDiffing();
  }

  public void addContacts(List<AccountRealm> accounts) {
    for (AccountRealm account : accounts) {
      models.add(new ContactModel_()
          .account(account)
          .onProfileClickListener(onProfileClickListener));
    }

    notifyModelsChanged();
  }
}
