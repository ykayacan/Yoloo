package com.yoloo.android.data.feed;

import android.support.annotation.NonNull;
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
  @Nonnull String getId();

  /**
   * Gets item.
   *
   * @return the item
   */
  @NonNull @Nonnull M getItem();
}
