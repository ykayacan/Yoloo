package com.yoloo.android.data.feed;

import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.util.Objects;
import java.util.List;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

/**
 * The type Recommended group list feed item.
 */
public final class RecommendedGroupListFeedItem implements FeedItem<List<GroupRealm>> {

  private final List<GroupRealm> groups;

  /**
   * Instantiates a new Recommended group list feed item.
   *
   * @param groups the groups
   */
  public RecommendedGroupListFeedItem(@Nonnull List<GroupRealm> groups) {
    checkNotNull(groups, "groups cannot be null");
    this.groups = groups;
  }

  @Nonnull @Override public String getId() {
    return RecommendedGroupListFeedItem.class.getName();
  }

  @Nonnull @Override public List<GroupRealm> getItem() {
    return groups;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RecommendedGroupListFeedItem that = (RecommendedGroupListFeedItem) o;
    return Objects.equal(groups, that.groups);
  }

  @Override public int hashCode() {
    return Objects.hashCode(groups);
  }

  @Override public String toString() {
    return "RecommendedGroupListFeedItem{" +
        "groups=" + groups +
        '}';
  }
}
