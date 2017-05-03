package com.yoloo.android.feature.follow;

import android.content.Context;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class FollowEpoxyController extends Typed2EpoxyController<List<AccountRealm>, Boolean> {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  private final CropCircleTransformation cropCircleTransformation;

  @AutoModel LoaderModel loaderModel;

  public FollowEpoxyController(Context context, OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    this.cropCircleTransformation = new CropCircleTransformation(context);
  }

  @Override
  protected void buildModels(List<AccountRealm> accountRealms, Boolean loadingMore) {
    Stream.of(accountRealms).forEach(this::createUserModel);

    loaderModel.addIf(loadingMore, this);
  }

  private void createUserModel(AccountRealm account) {
    new UserModel_()
        .id(account.getId())
        .account(account)
        .cropCircleTransformation(cropCircleTransformation)
        .onProfileClickListener(onProfileClickListener)
        .onFollowClickListener(onFollowClickListener)
        .addTo(this);
  }
}
