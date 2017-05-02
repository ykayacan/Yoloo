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
import com.googlecode.objectify.condition.IfFalse;
import com.googlecode.objectify.condition.IfNotDefault;
import com.googlecode.objectify.condition.IfNotZero;
import com.googlecode.objectify.condition.IfNull;
import com.googlecode.objectify.condition.IfTrue;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.post.transformer.PostTransformer;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(PostTransformer.class)
public class PostEntity implements Votable {

  public static final String FIELD_CREATED = "created";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_GROUP_KEY = "travelerGroup";
  public static final String FIELD_COMMENTED = "commented";
  public static final String FIELD_RANK = "rank";
  public static final String FIELD_BOUNTY = "bounty";
  public static final String FIELD_POST_TYPE = "postType";
  public static final String FIELD_HAS_MEDIA = "hasMedia";

  @Id private long id;

  @Parent private Key<Account> parent;

  @Wither private Link avatarUrl;

  @Wither private String username;

  @Wither private String content;

  @Wither @IgnoreSave(IfNull.class) private String title;

  @Load(ShardGroup.class) private List<Ref<PostShard>> shardRefs;

  @Singular @IgnoreSave(IfNull.class) private Set<Key<Account>> reportedByKeys;

  @Wither @IgnoreSave(IfNull.class) private Key<Comment> acceptedCommentKey;

  /**
   * The bounty value for the question. Bounty listFeed is given below. 10, 20, 30, 40, 50
   */
  @Index(IfNotZero.class) @Wither private int bounty;

  @Index(IfTrue.class) @Wither private boolean hasMedia;

  @Wither @IgnoreSave(IfNull.class) private List<PostMedia> medias;

  @Index @Wither private Set<String> tags;

  @Index @Wither private Key<TravelerGroupEntity> travelerGroup;

  // A performance optimization for gamification system.
  // If post is already commented, then mark as true.
  @Index @IgnoreSave(IfFalse.class) @Wither private Boolean commented;

  @Index(IfNotDefault.class) @Wither private double rank;

  @Index private int postType;

  @Index private DateTime created;

  // Extra fields

  @Wither @Ignore private Vote.Direction dir;

  @Wither @Ignore private long voteCount;

  @Wither @Ignore private long commentCount;

  @Wither @Ignore private int reportCount;

  @Wither @Ignore private boolean bookmarked;

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

  public boolean isCommented() {
    return commented != null;
  }

  @Nullable
  public String getAcceptedCommentId() {
    if (acceptedCommentKey == null) {
      return null;
    }
    return acceptedCommentKey.toWebSafeString();
  }

  @Override
  public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override
  public Votable setVoteDir(Vote.Direction dir) {
    return PostEntity
        .builder()
        .id(id)
        .parent(parent)
        .avatarUrl(avatarUrl)
        .username(username)
        .title(title)
        .content(content)
        .shardRefs(shardRefs)
        .tags(tags)
        .travelerGroup(travelerGroup)
        .dir(dir)
        .bounty(bounty)
        .acceptedCommentKey(acceptedCommentKey)
        .medias(medias)
        .commentCount(commentCount)
        .voteCount(voteCount)
        .reportCount(reportCount)
        .commented(commented)
        .bookmarked(bookmarked)
        .postType(postType)
        .created(created)
        .build();
  }

  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(shardRefs);
  }

  @OnLoad
  void onLoad() {
    /*voteCount = 0L;
    commentCount = 0L;
    reportCount = 0;
    dir = Vote.Direction.DEFAULT;*/
  }

  @AllArgsConstructor
  @Getter
  public enum Type {
    TEXT_POST(0), RICH_POST(1), BLOG(2), IMAGE_POST(3);

    private int type;
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }

  @Value
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  @FieldDefaults(makeFinal = false)
  public static class PostMedia {
    private String mediaId;
    private String url;

    public static PostMedia from(MediaEntity media) {
      return new PostMedia(media.getWebsafeId(), media.getUrl());
    }
  }
}