package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.AccountRealm;
import java.util.UUID;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type New user welcome feed item.
 */
public class NewUserWelcomeFeedItem implements FeedItem<AccountRealm> {

  private final AccountRealm account;

  /**
   * Instantiates a new New user welcome feed item.
   *
   * @param account the account
   */
  public NewUserWelcomeFeedItem(@Nonnull AccountRealm account) {
    checkNotNull(account, "post cannot be null");
    this.account = account;
  }

  @Nonnull @Override public String id() {
    return UUID.randomUUID().toString();
  }

  @Nonnull @Override public AccountRealm getItem() {
    return account;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NewUserWelcomeFeedItem)) return false;

    NewUserWelcomeFeedItem that = (NewUserWelcomeFeedItem) o;

    return account.equals(that.account);
  }

  @Override public int hashCode() {
    return account.hashCode();
  }

  @Override public String toString() {
    return "NewUserWelcomeFeedItem{" +
        "account=" + account +
        '}';
  }
}
