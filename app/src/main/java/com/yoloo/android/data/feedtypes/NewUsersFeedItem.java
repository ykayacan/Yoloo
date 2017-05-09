package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.db.AccountRealm;
import java.util.List;

public class NewUsersFeedItem implements FeedItem {
  private final List<AccountRealm> users;

  public NewUsersFeedItem(List<AccountRealm> users) {
    this.users = users;
  }

  public List<AccountRealm> getUsers() {
    return users;
  }
}
