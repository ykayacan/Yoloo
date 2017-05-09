package com.yoloo.android.feature.search;

import android.content.Context;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class SearchUserEpoxyController extends TypedEpoxyController<List<AccountRealm>> {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  private final CropCircleTransformation circleTransformation;

  SearchUserEpoxyController(Context context, OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;
    this.circleTransformation = new CropCircleTransformation(context);
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts) {
    Stream.of(accounts).forEach(this::createUserModel);
  }

  private void createUserModel(AccountRealm account) {
    new UserModel_()
        .id(account.getId())
        .account(account)
        .cropCircleTransformation(circleTransformation)
        .onProfileClickListener(onProfileClickListener)
        .onFollowClickListener(onFollowClickListener)
        .addTo(this);
  }
}
