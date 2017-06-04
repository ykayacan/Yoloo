package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.PostRealm;
import java.util.List;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type Trending blog list feed item.
 */
public final class TrendingBlogListFeedItem implements FeedItem<List<PostRealm>> {

  private final List<PostRealm> posts;

  /**
   * Instantiates a new Trending blog list feed item.
   *
   * @param posts the posts
   */
  public TrendingBlogListFeedItem(@Nonnull List<PostRealm> posts) {
    checkNotNull(posts, "posts cannot be null");
    this.posts = posts;
  }

  @Nonnull @Override public String getId() {
    return TrendingBlogListFeedItem.class.getName();
  }

  @Nonnull @Override public List<PostRealm> getItem() {
    return posts;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrendingBlogListFeedItem)) return false;

    TrendingBlogListFeedItem that = (TrendingBlogListFeedItem) o;

    return posts.equals(that.posts);
  }

  @Override public int hashCode() {
    return posts.hashCode();
  }

  @Override public String toString() {
    return "TrendingBlogListFeedItem{" +
        "posts=" + posts +
        '}';
  }
}
