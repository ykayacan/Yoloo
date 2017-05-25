package com.yoloo.android.feature.chat.createchat;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.search.UserModel;
import com.yoloo.android.feature.search.UserModel_;
import java.util.List;

class CreateChatEpoxyController extends TypedEpoxyController<List<AccountRealm>> {

  private final UserModel.OnUserClickListener onUserClickListener;

  CreateChatEpoxyController(UserModel.OnUserClickListener onUserClickListener) {
    this.onUserClickListener = onUserClickListener;
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts) {
    Stream
        .of(accounts)
        .forEach(account -> new UserModel_()
            .id(account.getId())
            .account(account)
            .onUserClickListener(onUserClickListener)
            .addTo(this));
  }
}
