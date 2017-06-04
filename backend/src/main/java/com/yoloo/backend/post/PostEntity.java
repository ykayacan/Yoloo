package com.yoloo.backend.post;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfFalse;
import com.googlecode.objectify.condition.IfNotDefault;
import com.googlecode.objectify.condition.IfNotEmpty;
import com.googlecode.objectify.condition.IfNotNull;
import com.googlecode.objectify.condition.IfNotZero;
import com.googlecode.objectify.condition.IfNull;
import com.googlecode.objectify.condition.IfTrue;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.algorithm.RankVisitor;
import com.yoloo.backend.algorithm.Rankable;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.feed.FeedShard;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.post.transformer.PostTransformer;
import com.yoloo.backend.shard.Shardable;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Data
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(PostTransformer.class)
public class PostEntity implements Votable, Rankable {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_GROUP_KEY = "travelerGroup";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";
  public static final String FIELD_BOUNTY = "bounty";
  public static final String FIELD_POST_TYPE = "postType";
  public static final String FIELD_HAS_MEDIA = "hasMedia";
  public static final String FIELD_ACCEPTED_COMMENT_KEY = "acceptedCommentKey";

  @Id private long id;

  @Index private int postType;

  @Parent private Key<Account> parent;

  @Wither
  @AlsoLoad("avatarUrl")
  private String ownerAvatarUrl;

  @Wither
  @AlsoLoad("username")
  private String ownerUsername;

  @Wither private String content;

  @Wither
  @IgnoreSave(IfNull.class)
  private String title;

  @Load(ShardGroup.class) private List<Ref<PostShard>> shardRefs;

  @Wither
  @Index(IfNotNull.class)
  private Key<Comment> acceptedCommentKey;

  private GeoPt location;

  @Index
  @Wither
  private Set<String> tags;

  /**
   * The bounty value for the question.
   * Bounty listFeed is given below.
   * 10, 20, 30, 40, 50
   */
  @Index(IfNotZero.class)
  @Wither
  private int bounty;

  @Index(IfTrue.class)
  @Wither
  private boolean hasMedia;

  @Wither private List<PostMedia> medias;

  @Index
  @Wither
  @AlsoLoad("travelerGroup")
  private Key<TravelerGroupEntity> travelerGroupKey;

  // A performance optimization for gamification system.
  // If post is already commented, then mark as true.
  @Index
  @IgnoreSave(IfFalse.class)
  @Wither
  private boolean commented;

  @Index(IfNotDefault.class)
  @Wither
  private double rank;

  @Index private DateTime created;

  // Extra fields

  @Wither @Ignore private Vote.Direction dir;

  @Wither @Ignore private long voteCount;

  @Wither @Ignore private long commentCount;

  @Wither @Ignore private boolean bookmarked;

  @Wither @Ignore private boolean owner;

  // Don't use this
  @Wither @Ignore private Map<Ref<PostShard>, PostShard> shardMap;

  // Methods

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<PostEntity> getKey() {
    return Key.create(parent, getClass(), id);
  }

  @Nullable public String getAcceptedCommentId() {
    return acceptedCommentKey == null ? null : acceptedCommentKey.toWebSafeString();
  }

  @Override public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override public Votable setVoteDir(Vote.Direction dir) {
    return PostEntity
        .builder()
        .id(id)
        .parent(parent)
        .ownerAvatarUrl(ownerAvatarUrl)
        .ownerUsername(ownerUsername)
        .title(title)
        .content(content)
        .shardRefs(shardRefs)
        .acceptedCommentKey(acceptedCommentKey)
        .bounty(bounty)
        .hasMedia(hasMedia)
        .medias(medias)
        .tags(tags)
        .travelerGroupKey(travelerGroupKey)
        .commented(commented)
        .rank(rank)
        .postType(postType)
        .created(created)
        .dir(dir)
        .voteCount(voteCount)
        .commentCount(commentCount)
        .bookmarked(bookmarked)
        .owner(owner)
        .shardMap(shardMap)
        .location(location)
        .build();
  }

  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(shardRefs);
  }

  @Override public void acceptRank(RankVisitor visitor) {
    visitor.visit(this);
  }

  @AllArgsConstructor
  @Getter
  public enum Type {
    TEXT_POST(0),
    RICH_POST(1),
    BLOG_POST(2);

    private int type;
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }

  @Data
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  public static class PostMedia {
    private String mediaId;
    private String url;

    public static PostMedia from(MediaEntity media) {
      return new PostMedia(media.getWebsafeId(), media.getUrl());
    }
  }

  @Entity
  @Cache(expirationSeconds = 120)
  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class PostShard implements FeedShard, Shardable.Shard {

    /**
     * Websafe postId:shard_num
     */
    @Id private String id;

    @AlsoLoad("comments") private long commentCount;

    @AlsoLoad("votes") private long voteCount;

    @Index(IfNotEmpty.class) private Set<Key<Bookmark>> bookmarkKeys;

    public static Key<PostShard> createKey(Key<PostEntity> postKey, int shardNum) {
      return Key.create(PostShard.class, postKey.toWebSafeString() + ":" + shardNum);
    }

    public Key<PostEntity> getPostKey() {
      return Key.create(id.split(":")[0]);
    }

    public Key<PostShard> getKey() {
      return Key.create(PostShard.class, this.id);
    }

    public void increaseCommentCount() {
      ++commentCount;
    }

    public void decreaseCommentCount() {
      --commentCount;
    }

    public PostShard mergeWith(PostShard shard) {
      this.commentCount += shard.commentCount;
      this.voteCount += shard.voteCount;
      return this;
    }

    @Override public void increaseVotesBy(long value) {
      voteCount += value;
    }

    @Override public void decreaseVotesBy(long value) {
      voteCount -= value;
    }
  }
}