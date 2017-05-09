package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.CommentRealm;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type Comment feed item.
 */
public final class CommentFeedItem implements FeedItem<CommentRealm> {

  private final CommentRealm comment;

  /**
   * Instantiates a new Comment feed item.
   *
   * @param comment the comment
   */
  public CommentFeedItem(@Nonnull CommentRealm comment) {
    checkNotNull(comment, "comment cannot be null");
    this.comment = comment;
  }

  @Nonnull @Override public String id() {
    return comment.getId();
  }

  @Nonnull @Override public CommentRealm getItem() {
    return comment;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentFeedItem)) return false;

    CommentFeedItem that = (CommentFeedItem) o;

    return comment.equals(that.comment);
  }

  @Override public int hashCode() {
    return comment.hashCode();
  }

  @Override public String toString() {
    return "CommentFeedItem{" +
        "comment=" + comment +
        '}';
  }
}
