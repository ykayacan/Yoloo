package com.yoloo.android.data.feed;

import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type Bounty button feed item.
 */
public final class BountyButtonFeedItem implements FeedItem<String> {

  private final String buttonName;

  /**
   * Instantiates a new Bounty button feed item.
   *
   * @param buttonName the button name
   */
  public BountyButtonFeedItem(@Nonnull String buttonName) {
    checkNotNull(buttonName, "buttonName cannot be null");
    this.buttonName = buttonName;
  }

  @Nonnull @Override public String id() {
    return getClass().getName();
  }

  @Nonnull @Override public String getItem() {
    return buttonName;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BountyButtonFeedItem)) return false;

    BountyButtonFeedItem that = (BountyButtonFeedItem) o;

    return buttonName.equals(that.buttonName);
  }

  @Override public int hashCode() {
    return buttonName.hashCode();
  }

  @Override public String toString() {
    return "BountyButtonFeedItem{" +
        "buttonName='" + buttonName + '\'' +
        '}';
  }
}
