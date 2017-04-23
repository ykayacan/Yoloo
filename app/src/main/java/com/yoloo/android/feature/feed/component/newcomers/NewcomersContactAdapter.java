package com.yoloo.android.feature.feed.component.newcomers;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class NewcomersContactAdapter extends EpoxyAdapter {

  private OnFollowClickListener onFollowClickListener;
  private OnItemClickListener<AccountRealm> onItemClickListener;

  public void setOnFollowClickListener(OnFollowClickListener listener) {
    this.onFollowClickListener = listener;
  }

  public void setOnItemClickListener(OnItemClickListener<AccountRealm> listener) {
    this.onItemClickListener = listener;
  }

  void addNewcomersContacts(List<AccountRealm> items) {
    addModels(createModels(items));
  }

  private List<NewcomersContactModel_> createModels(List<AccountRealm> items) {
    if (onItemClickListener == null) {
      throw new IllegalStateException("onItemClickListener is null.");
    }

    if (onFollowClickListener == null) {
      throw new IllegalStateException("onFollowClickListener is null.");
    }

    return Stream
        .of(items)
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
