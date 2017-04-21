package com.yoloo.android.feature.group.groupuserslist;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class GroupUsersListAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  private final CropCircleTransformation circleTransformation;

  GroupUsersListAdapter(Context context, OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    enableDiffing();

    circleTransformation = new CropCircleTransformation(context);
  }

  void addUsers(List<AccountRealm> accounts) {
    models.clear();

    models.addAll(Stream
        .of(accounts)
        .map(account -> new UserModel_()
            .account(account)
            .cropCircleTransformation(circleTransformation)
            .onProfileClickListener(onProfileClickListener)
            .onFollowClickListener(onFollowClickListener))
        .collect(Collectors.toList()));

    notifyModelsChanged();
  }
}
