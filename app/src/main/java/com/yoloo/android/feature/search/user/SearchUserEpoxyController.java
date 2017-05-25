package com.yoloo.android.feature.search.user;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.search.UserModel_;
import java.util.List;

class SearchUserEpoxyController extends TypedEpoxyController<List<AccountRealm>> {

  private final OnProfileClickListener onProfileClickListener;

  SearchUserEpoxyController(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts) {
    Stream.of(accounts).forEach(this::createUserModel);
  }

  private void createUserModel(AccountRealm account) {
    new UserModel_()
        .id(account.getId())
        .account(account)
        .onProfileClickListener(onProfileClickListener)
        .addTo(this);
  }
}
