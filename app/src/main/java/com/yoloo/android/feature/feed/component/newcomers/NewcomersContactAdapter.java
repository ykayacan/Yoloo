package com.yoloo.android.feature.feed.component.newcomers;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class NewcomersContactAdapter extends EpoxyAdapter {

  private final OnFollowClickListener onFollowClickListener;
  private final OnItemClickListener<AccountRealm> onItemClickListener;

  public NewcomersContactAdapter(OnFollowClickListener onFollowClickListener,
      OnItemClickListener<AccountRealm> onItemClickListener) {
    this.onFollowClickListener = onFollowClickListener;
    this.onItemClickListener = onItemClickListener;
  }

  void addNewcomersContacts(List<AccountRealm> items) {
    addModels(createModels(items, onItemClickListener, onFollowClickListener));
  }

  private List<NewcomersContactModel_> createModels(List<AccountRealm> items,
      OnItemClickListener<AccountRealm> onItemClickListener,
      OnFollowClickListener onFollowClickListener) {

    return Stream.of(items)
        .map(account -> new NewcomersContactModel_()
            .account(account)
            .onItemClickListener(onItemClickListener)
            .onFollowClickListener(onFollowClickListener))
        .toList();
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }
}
