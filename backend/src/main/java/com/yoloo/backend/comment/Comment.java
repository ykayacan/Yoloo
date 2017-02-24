package com.yoloo.backend.comment;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.transformer.CommentTransformer;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(CommentTransformer.class)
public class Comment implements Votable {

  public static final String FIELD_QUESTION_KEY = "postKey";
  public static final String FIELD_CREATED = "created";
  public static final String FIELD_ACCEPTED = "accepted";

  @Id
  private long id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @Wither
  @NonFinal
  private String username;

  @Wither
  @NonFinal
  private Link avatarUrl;

  @Wither
  @NonFinal
  private String content;

  @Load(ShardGroup.class)
  @NonFinal
  private List<Ref<CommentShard>> shardRefs;

  @Index
  @NonFinal
  private Key<Post> postKey;

  @Wither
  @NonFinal
  @Index(IfNotDefault.class)
  private boolean accepted;

  @Index
  @NonFinal
  private DateTime created;

  // Extra fields

  @Ignore
  @Wither
  @NonFinal
  private Vote.Direction dir;

  @Ignore
  @Wither
  @NonFinal
  private long voteCount;

  public Key<Comment> getKey() {
    return Key.create(parent, Comment.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  @Override
  public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @Override public Votable setVoteDir(Vote.Direction dir) {
    return Comment.builder()
        .id(id)
        .parent(parent)
        .postKey(postKey)
        .shardRefs(shardRefs)
        .content(content)
        .username(username)
        .avatarUrl(avatarUrl)
        .dir(dir)
        .accepted(accepted)
        .voteCount(voteCount)
        .created(created)
        .build();
  }

  public List<CommentShard> getShards() {
    return Deref.deref(this.shardRefs);
  }

  @OnLoad void onLoad() {
    voteCount = 0L;
    dir = Vote.Direction.DEFAULT;
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}