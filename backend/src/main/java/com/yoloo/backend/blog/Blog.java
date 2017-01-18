package com.yoloo.backend.blog;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.feed.FeedItem;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Vote;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Blog implements FeedItem {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_CATEGORIES = "categories";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";

  @Id
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Parent
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> parentUserKey;

  @Wither
  @NonFinal
  private Link avatarUrl;

  @Wither
  @NonFinal
  private String username;

  @Wither
  @NonFinal
  private String content;

  @NonFinal
  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<BlogCounterShard>> shardRefs;

  @NonFinal
  @Singular
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Set<Key<Account>> reportedByKeys;

  @Wither
  @NonFinal
  private String title;

  @Index
  @Wither
  @NonFinal
  private Set<String> tags;

  @Index
  @Wither
  @NonFinal
  private Set<String> categories;

  @Wither
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Comment> acceptedCommentKey;

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
  @NonFinal
  private Media media;

  @Index
  @NonFinal
  private DateTime created;

  // Extra fields

  @Wither
  @NonFinal
  @Ignore
  private Vote.Direction dir;

  @Wither
  @NonFinal
  @Ignore
  private long votes;

  @Wither
  @NonFinal
  @Ignore
  private long comments;

  @Wither
  @NonFinal
  @Ignore
  private int reports;

  // Methods

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Blog> getKey() {
    return Key.create(parentUserKey, getClass(), id);
  }

  public String getAcceptedCommentId() {
    return acceptedCommentKey.toWebSafeString();
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
    return (List<E>) Deref.deref(this.shardRefs);
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}
