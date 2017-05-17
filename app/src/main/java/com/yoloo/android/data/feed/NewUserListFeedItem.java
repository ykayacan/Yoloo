package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.AccountRealm;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type New user list feed item.
 */
public final class NewUserListFeedItem implements FeedItem<List<AccountRealm>> {

  private final List<AccountRealm> accounts;

  /**
   * Instantiates a new New user list feed item.
   *
   * @param accounts the accounts
   */
  public NewUserListFeedItem(@Nonnull List<AccountRealm> accounts) {
    checkNotNull(accounts, "accounts cannot be null");
    this.accounts = accounts;
  }

  @Nonnull @Override public String id() {
    return UUID.randomUUID().toString();
  }

  @Nonnull @Override public List<AccountRealm> getItem() {
    return accounts;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NewUserListFeedItem)) return false;

    NewUserListFeedItem that = (NewUserListFeedItem) o;

    return accounts.equals(that.accounts);
  }

  @Override public int hashCode() {
    return accounts.hashCode();
  }

  @Override public String toString() {
    return "NewUserListFeedItem{" +
        "accounts=" + accounts +
        '}';
  }
}
