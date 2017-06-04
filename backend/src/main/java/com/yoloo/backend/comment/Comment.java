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
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.transformer.CommentTransformer;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.util.Deref;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(CommentTransformer.class)
public class Comment implements Votable {

  public static final String FIELD_POST_KEY = "postKey";
  public static final String FIELD_CREATED = "created";

  @Id private long id;

  @Parent private Key<Account> parent;

  @Wither private String username;

  @Wither private Link avatarUrl;

  @Wither private String content;

  @Load(ShardGroup.class) private List<Ref<CommentShard>> shardRefs;

  @Index private Key<PostEntity> postKey;

  @Index private DateTime created;

  @Wither private boolean accepted;

  // Extra fields

  @Ignore @Wither private int dir;

  @Ignore @Wither private long voteCount;

  @Wither @Ignore private boolean owner;

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

  @Override
  public Votable setVoteDir(Vote.Direction dir) {
    return Comment
        .builder()
        .id(id)
        .parent(parent)
        .postKey(postKey)
        .shardRefs(shardRefs)
        .content(content)
        .username(username)
        .avatarUrl(avatarUrl)
        .dir(dir.getValue())
        .voteCount(voteCount)
        .created(created)
        .accepted(accepted)
        .owner(owner)
        .build();
  }

  public List<CommentShard> getShards() {
    return Deref.deref(shardRefs);
  }

  @NoArgsConstructor
  public static class ShardGroup {
  }
}