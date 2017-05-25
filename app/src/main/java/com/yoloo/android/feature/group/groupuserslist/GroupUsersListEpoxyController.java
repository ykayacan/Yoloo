package com.yoloo.android.feature.group.groupuserslist;

import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import java.util.ArrayList;
import java.util.List;

class GroupUsersListEpoxyController extends Typed2EpoxyController<List<AccountRealm>, Boolean> {

  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  @AutoModel LoaderModel loaderModel;

  private List<AccountRealm> items;

  GroupUsersListEpoxyController(OnProfileClickListener onProfileClickListener,
      OnFollowClickListener onFollowClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    this.items = new ArrayList<>();
  }

  void showLoader() {
    setData(items, true);
  }

  void hideLoader() {
    setData(items, false);
  }

  void setLoadMoreData(List<AccountRealm> items) {
    this.items.addAll(items);
    setData(this.items, false);
  }

  @Override public void setData(List<AccountRealm> items, Boolean loadingMore) {
    this.items = items;
    super.setData(this.items, loadingMore);
  }

  @Override protected void buildModels(List<AccountRealm> accounts, Boolean loadingMore) {
    Stream.of(accounts).forEach(this::createUserModel);

    loaderModel.addIf(loadingMore, this);
  }

  private void createUserModel(AccountRealm account) {
    new UserModel_()
        .id(account.getId())
        .account(account)
        .onProfileClickListener(onProfileClickListener)
        .onFollowClickListener(onFollowClickListener)
        .addTo(this);
  }
}
