package com.yoloo.android.feature.explore.data;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.util.Objects;
import java.util.List;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

public class RecentMediaListItem implements FeedItem<List<PostRealm>> {

  private final List<PostRealm> posts;

  public RecentMediaListItem(@Nonnull List<PostRealm> posts) {
    checkNotNull(posts, "posts cannot be null");
    this.posts = posts;
  }

  @Nonnull @Override public String getId() {
    return RecentMediaListItem.class.getName();
  }

  @NonNull @Override public List<PostRealm> getItem() {
    return posts;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RecentMediaListItem that = (RecentMediaListItem) o;
    return Objects.equal(posts, that.posts);
  }

  @Override public int hashCode() {
    return Objects.hashCode(posts);
  }

  @Override public String toString() {
    return "RecentMediaListItem{" +
        "posts=" + posts +
        '}';
  }
}
