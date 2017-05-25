package com.yoloo.android.feature.recommenduser;

import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import java.util.ArrayList;
import java.util.List;

class RecommendUserEpoxyController extends Typed2EpoxyController<List<AccountRealm>, Void> {

  private OnFollowClickListener onFollowClickListener;

  private List<AccountRealm> models = new ArrayList<>();

  void setOnFollowClickListener(OnFollowClickListener onFollowClickListener) {
    this.onFollowClickListener = onFollowClickListener;
  }

  @Override
  public void setData(List<AccountRealm> models, Void data2) {
    this.models = models;
    super.setData(models, data2);
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts, Void aVoid) {
    Stream.of(accounts).forEach(this::createUserModel);
  }

  private void createUserModel(AccountRealm account) {
    new UserModel_()
        .id(account.getId())
        .account(account)
        .showFollowButton(true)
        .onFollowClickListener(onFollowClickListener)
        .addTo(this);
  }

  public void remove(AccountRealm account) {
    models.remove(account);
    setData(models, null);
  }
}
