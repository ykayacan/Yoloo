package com.yoloo.android.feature.chat.compose;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class ComposeAdapter extends EpoxyAdapter {

  private final Context context;

  private final OnProfileClickListener onProfileClickListener;

  public ComposeAdapter(Context context, OnProfileClickListener onProfileClickListener) {
    this.context = context;

    this.onProfileClickListener = onProfileClickListener;

    enableDiffing();
  }

  public void addContacts(List<AccountRealm> accounts) {
    for (AccountRealm account : accounts) {
      models.add(new ContactModel_()
          .account(account)
          .onProfileClickListener(onProfileClickListener)
          .cropCircleTransformation(new CropCircleTransformation(context)));
    }

    notifyModelsChanged();
  }
}
