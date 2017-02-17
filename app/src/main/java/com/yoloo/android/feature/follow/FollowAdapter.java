package com.yoloo.android.feature.follow;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class FollowAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  private final Context context;

  private final CropCircleTransformation cropCircleTransformation;

  public FollowAdapter(Context context, OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.context = context;
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    enableDiffing();

    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void addUsers(List<AccountRealm> accounts) {
    for (AccountRealm account : accounts) {
      models.add(new UserModel_()
          .account(account)
          .cropCircleTransformation(cropCircleTransformation)
          .onProfileClickListener(onProfileClickListener)
          .onFollowClickListener(onFollowClickListener));
    }

    notifyModelsChanged();
  }

  public void clear() {
    models.clear();
  }
}
