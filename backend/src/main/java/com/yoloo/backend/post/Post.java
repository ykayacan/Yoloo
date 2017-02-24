package com.yoloo.backend.post;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.googlecode.objectify.condition.IfNotZero;
import com.googlecode.objectify.condition.IfNull;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.post.transformer.PostTransformer;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(PostTransformer.class)
public class Post implements Votable {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_CATEGORIES = "categories";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";
  public static final String FIELD_BOUNTY = "bounty";
  public static final String FIELD_POST_TYPE = "postType";

  @Id
  private long id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @Wither
  @NonFinal
  private Link avatarUrl;

  @Wither
  @NonFinal
  private String username;

  @Wither
  @NonFinal
  private String content;

  @Wither
  @NonFinal
  @IgnoreSave(IfNull.class)
  private String title;

  @Load(ShardGroup.class)
  @NonFinal
  private List<Ref<PostShard>> shardRefs;

  @Singular
  @NonFinal
  @IgnoreSave(IfNull.class)
  private Set<Key<Account>> reportedByKeys;

  @Wither
  @NonFinal
  @IgnoreSave(IfNull.class)
  private Key<Comment> acceptedCommentKey;

  /**
   * The bounty value for the question. Bounty listFeed is given below. 10, 20, 30, 40, 50
   */
  @Index(IfNotZero.class)
  @Wither
  @NonFinal
  private int bounty;

  @Wither
  @NonFinal
  @IgnoreSave(IfNull.class)
  private Media media;

  @Index
  @Wither
  @NonFinal
  private Set<String> tags;

  @Index
  @Wither
  @NonFinal
  private Set<String> categories;

  // A performance optimization for gamification system.
  // If post is already commented, then mark as true.
  @Index
  @IgnoreSave(IfNull.class)
  @Wither
  @NonFinal
  private Boolean commented;

  @Index(IfNotDefault.class)
  @Wither
  @NonFinal
  private double rank;

  @Index
  @NonFinal
  private PostType postType;

  @Index
  @NonFinal
  private DateTime created;

  // Extra fields

  @Wither
  @Ignore
  @NonFinal
  private Vote.Direction dir;

  @Wither
  @Ignore
  @NonFinal
  private long voteCount;

  @Wither
  @Ignore
  @NonFinal
  private long commentCount;

  @Wither
  @Ignore
  @NonFinal
  private int reportCount;

  // Methods

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<Post> getKey() {
    return Key.create(parent, getClass(), id);
  }

  public boolean isCommented() {
    return commented != null;
  }

  @Nullable public String getAcceptedCommentId() {
    if (acceptedCommentKey == null) {
      return null;
    }
    return acceptedCommentKey.toWebSafeString();
  }

  @Override public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override public Votable setVoteDir(Vote.Direction dir) {
    return Post.builder()
        .id(id)
        .parent(parent)
        .avatarUrl(avatarUrl)
        .username(username)
        .title(title)
        .content(content)
        .shardRefs(shardRefs)
        .tags(tags)
        .categories(categories)
        .dir(dir)
        .bounty(bounty)
        .acceptedCommentKey(acceptedCommentKey)
        .media(media)
        .commentCount(commentCount)
        .voteCount(voteCount)
        .reportCount(reportCount)
        .commented(commented)
        .postType(postType)
        .created(created)
        .build();
  }

  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(shardRefs);
  }

  @OnLoad void onLoad() {
    voteCount = 0L;
    commentCount = 0L;
    reportCount = 0;
    dir = Vote.Direction.DEFAULT;
  }

  public enum PostType {
    QUESTION,
    BLOG
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}