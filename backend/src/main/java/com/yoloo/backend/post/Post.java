package com.yoloo.backend.post;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfFalse;
import com.googlecode.objectify.condition.IfNotNull;
import com.googlecode.objectify.condition.IfNotZero;
import com.googlecode.objectify.condition.IfZero;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.util.Deref;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.joda.time.DateTime;

@AllArgsConstructor
@NoArgsConstructor
public abstract class Post {

  @Id protected long id;

  @Index protected PostType postType;

  @Parent protected Key<Account> parent;

  protected String ownerAvatarUrl;

  protected String ownerUsername;

  protected String body;

  @Load(ShardGroup.class) protected List<Ref<PostShard>> shardRefs;

  @Nullable @Index(IfNotNull.class) protected Key<Comment> acceptedCommentKey;

  @Index protected Set<String> tags;

  @Index protected Key<TravelerGroupEntity> travelerGroup;

  // A performance optimization for gamification system.
  // If post is already commented, then mark as true.
  @Index(IfFalse.class) protected boolean commented;

  @Index(IfZero.class) protected double rank;

  @Index protected DateTime created;

  // Extra fields

  @Ignore protected int dir;

  @Ignore protected long voteCount;

  @Ignore protected long commentCount;

  @Ignore protected boolean bookmarked;

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

  public <E> List<E> getShards() {
    //noinspection unchecked
    return (List<E>) Deref.deref(shardRefs);
  }

  @Getter
  @AllArgsConstructor
  public enum PostType {
    TEXT_POST(0), RICH_POST(1), BLOG(2), IMAGE_POST(3);

    private int type;
  }

  @Value
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
  @Entity
  public static class TextPost extends Post {

    /**
     * The bounty value for the question. Bounty list is given below. 10, 20, 30, 40, 50
     */
    @Index(IfNotZero.class) private int bounty;

    @Builder(toBuilder = true)
    private TextPost(
        long id,
        PostType postType,
        Key<Account> parent,
        String ownerAvatarUrl,
        String ownerUsername,
        String body,
        List<Ref<PostShard>> shardRefs,
        Key<Comment> acceptedCommentKey,
        Set<String> tags,
        Key<TravelerGroupEntity> travelerGroup,
        boolean commented,
        double rank,
        DateTime created,
        int dir,
        long voteCount,
        long commentCount,
        boolean bookmarked,
        int bounty) {
      super(
          id,
          postType,
          parent,
          ownerAvatarUrl,
          ownerUsername,
          body,
          shardRefs,
          acceptedCommentKey,
          tags,
          travelerGroup,
          commented,
          rank,
          created,
          dir,
          voteCount,
          commentCount,
          bookmarked);
      this.bounty = bounty;
    }
  }

  @Value
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  @Entity
  public static class RichPost extends Post {

    private List<PostMedia> medias;

    /**
     * The bounty value for the question. Bounty list is given below. 10, 20, 30, 40, 50
     */
    @Index(IfNotZero.class) private int bounty;

    @Builder(toBuilder = true)
    private RichPost(
        long id,
        PostType postType,
        Key<Account> parent,
        String ownerAvatarUrl,
        String ownerUsername,
        String body,
        List<Ref<PostShard>> shardRefs,
        Key<Comment> acceptedCommentKey,
        Set<String> tags,
        Key<TravelerGroupEntity> travelerGroup,
        boolean commented,
        double rank,
        DateTime created,
        int dir,
        long voteCount,
        long commentCount,
        boolean bookmarked,
        List<PostMedia> medias,
        int bounty) {
      super(
          id,
          postType,
          parent,
          ownerAvatarUrl,
          ownerUsername,
          body,
          shardRefs,
          acceptedCommentKey,
          tags,
          travelerGroup,
          commented,
          rank,
          created,
          dir,
          voteCount,
          commentCount,
          bookmarked);
      this.medias = medias;
      this.bounty = bounty;
    }
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }

  @Value
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(force = true,
      access = AccessLevel.PRIVATE)
  @FieldDefaults(makeFinal = false)
  public static class PostMedia {
    private String mediaId;
    private String url;

    public static PostMedia from(MediaEntity media) {
      return new PostMedia(media.getWebsafeId(), media.getUrl());
    }
  }
}