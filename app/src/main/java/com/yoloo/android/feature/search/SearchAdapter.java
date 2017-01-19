package com.yoloo.android.feature.search;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import java.util.List;

public class SearchAdapter extends EpoxyAdapter {

  private final OnTagClickListener onTagClickListener;
  private final OnProfileClickListener onProfileClickListener;

  public SearchAdapter(OnTagClickListener onTagClickListener,
      OnProfileClickListener onProfileClickListener) {
    this.onTagClickListener = onTagClickListener;
    this.onProfileClickListener = onProfileClickListener;

    enableDiffing();
  }

  public void replaceTags(List<TagRealm> tags) {
    models.clear();

    for (TagRealm tag : tags) {
      models.add(new TagModel_()
          .tag(tag)
          .onTagClickListener(onTagClickListener));
    }

    notifyModelsChanged();
  }

  public void replaceUsers(List<AccountRealm> accounts) {
    models.clear();

    for (AccountRealm account : accounts) {
      models.add(new UserModel_()
          .account(account)
          .onProfileClickListener(onProfileClickListener));
    }

    notifyModelsChanged();
  }
}
