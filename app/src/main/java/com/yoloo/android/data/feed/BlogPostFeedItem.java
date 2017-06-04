package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.PostRealm;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type Blog feed item.
 */
public final class BlogPostFeedItem implements FeedItem<PostRealm> {

  private final PostRealm post;

  /**
   * Instantiates a new Blog feed item.
   *
   * @param post the post
   */
  public BlogPostFeedItem(@Nonnull PostRealm post) {
    checkNotNull(post, "post cannot be null");
    this.post = post;
  }

  @Nonnull @Override public String getId() {
    return post.getId();
  }

  @Nonnull @Override public PostRealm getItem() {
    return post;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BlogPostFeedItem)) return false;

    BlogPostFeedItem that = (BlogPostFeedItem) o;

    return post.equals(that.post);
  }

  @Override public int hashCode() {
    return post.hashCode();
  }

  @Override public String toString() {
    return "BlogPostFeedItem{" +
        "post=" + post +
        '}';
  }
}
