package com.yoloo.android.feature.models.newusers;

import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.NewUsersFeedItem;
import com.yoloo.android.data.model.AccountRealm;
import java.util.ArrayList;
import java.util.List;

public class NewUserListModelGroup extends EpoxyModelGroup {

  public NewUserListModelGroup(NewUsersFeedItem item, NewUserListModelGroupCallbacks callbacks,
      RequestManager glide) {
    super(R.layout.item_new_users_list, buildModels(item, callbacks, glide));
    id(item.getClass().getName());
  }

  private static List<EpoxyModel<?>> buildModels(NewUsersFeedItem item,
      NewUserListModelGroupCallbacks callbacks, RequestManager glide) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_header_text));
    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_more_text).onClick(
        v -> callbacks.onNewUserListHeaderClicked()));

    // inner group models
    List<NewUserModel_> newUserModels = Stream
        .of(item.getUsers())
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

  public interface NewUserListModelGroupCallbacks {
    void onNewUserListHeaderClicked();

    void onNewUserClicked(AccountRealm account);

    void onNewUserFollowClicked(AccountRealm account, int direction);
  }
}
