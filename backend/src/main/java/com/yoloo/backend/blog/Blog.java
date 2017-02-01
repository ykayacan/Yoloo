package com.yoloo.backend.blog;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.googlecode.objectify.condition.IfNull;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.feed.FeedItem;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.MiniSize;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Vote;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder(toBuilder = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Blog implements FeedItem {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_CATEGORIES = "categories";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Parent
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> parent;

  @Wither
  private Link avatarUrl;

  @Wither
  private String username;

  @Wither
  private String content;

  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<BlogCounterShard>> shardRefs;

  @Singular
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Set<Key<Account>> reportedByKeys;

  @Wither
  private String title;

  @Index
  @Wither
  @NonFinal
  private Set<String> tags;

  @Index
  @Wither
  @NonFinal
  private Set<String> categories;

  /**
   * If a user questions a comment for given post then commented is true otherwise false.
   */
  @Index(value = IfNotDefault.class)
  @Wither
  @NonFinal
  private boolean commented;

  @Index(value = IfNotDefault.class)
  @Wither
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private double rank;

  @Wither
  @IgnoreSave(IfNull.class)
  private Media media;

  @Index
  @NonFinal
  private DateTime created;

  // Extra fields

  @Wither
  @Ignore
  private Vote.Direction dir;

  @Wither
  @Ignore
  private long votes;

  @Wither
  @Ignore
  private long comments;

  @Wither
  @Ignore
  private int reports;

  // Methods

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @ApiResourceProperty(name = "ownerId")
  public String getWebsafeOwnerId() {
    return this.parent.toWebSafeString();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Blog> getKey() {
    return Key.create(parent, getClass(), id);
  }

  @Override
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(shardRefs);
  }

  public Media getMedia() {
    if (media != null) {
      String mediaUrl = media.getUrl();

      ImmutableList<Media.Size> sizes =
          ImmutableList.<Media.Size>builder().add(new ThumbSize(mediaUrl))
              .add(new MiniSize(mediaUrl))
              .add(new LowSize(mediaUrl))
              .add(new MediumSize(mediaUrl))
              .add(new LargeSize(mediaUrl))
              .build();

      return this.media.withSizes(sizes);
    }
    return null;
  }

  @ApiResourceProperty(name = "created")
  public Date getDate() {
    return created.toDate();
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}
