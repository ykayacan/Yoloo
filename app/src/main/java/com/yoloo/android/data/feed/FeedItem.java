package com.yoloo.android.data.feed;

import javax.annotation.Nonnull;

/**
 * The interface Feed item.
 *
 * @param <M> the type parameter
 */
public interface FeedItem<M> {

  /**
   * Id string.
   *
   * @return the string
   */
  @Nonnull String id();

  /**
   * Gets item.
   *
   * @return the item
   */
  @Nonnull M getItem();
}
