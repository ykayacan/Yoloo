package com.yoloo.android.feature.chat.createchat;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.UserModel;
import com.yoloo.android.feature.search.UserModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class CreateChatEpoxyController extends TypedEpoxyController<List<AccountRealm>> {

  private final CropCircleTransformation cropCircleTransformation;
  private final UserModel.OnUserClickListener onUserClickListener;

  public CreateChatEpoxyController(CropCircleTransformation cropCircleTransformation,
      UserModel.OnUserClickListener onUserClickListener) {
    this.cropCircleTransformation = cropCircleTransformation;
    this.onUserClickListener = onUserClickListener;
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts) {
    Stream
        .of(accounts)
        .forEach(account -> new UserModel_()
            .id(account.getId())
            .account(account)
            .cropCircleTransformation(cropCircleTransformation)
            .onUserClickListener(onUserClickListener)
            .addTo(this));
  }
}
