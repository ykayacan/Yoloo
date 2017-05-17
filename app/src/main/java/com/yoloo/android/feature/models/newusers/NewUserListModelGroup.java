package com.yoloo.android.feature.models.newusers;

import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.feed.NewUserListFeedItem;
import java.util.ArrayList;
import java.util.List;

public class NewUserListModelGroup extends EpoxyModelGroup {

  public NewUserListModelGroup(NewUserListFeedItem item, Callbacks callbacks,
      RequestManager glide) {
    super(R.layout.item_new_users_list, buildModels(item, callbacks, glide));
    id(item.id());
  }

  private static List<EpoxyModel<?>> buildModels(NewUserListFeedItem item,
      Callbacks callbacks, RequestManager glide) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_new_user_header_text));

    // inner group models
    List<NewUserModel_> newUserModels = Stream
        .of(item.getItem())
        .map(account -> new NewUserModel_()
            .id(account.getId())
            .account(account)
            .glide(glide)
            .onFollowClickListener(
                (v, account1, direction) -> callbacks.onNewUserFollowClicked(account, direction))
            .onClickListener(v -> callbacks.onNewUserClicked(account)))
        .toList();

    // inner recyclerview
    models.add(new NewUserListModel_().numItemsExpectedOnDisplay(5).models(newUserModels));

    return models;
  }

  public interface Callbacks {
    void onNewUserListHeaderClicked();

    void onNewUserClicked(AccountRealm account);

    void onNewUserFollowClicked(AccountRealm account, int direction);
  }
}
