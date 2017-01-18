package com.yoloo.backend.comment;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
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
import com.yoloo.backend.question.Question;
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
public class Comment implements Votable {

  public static final String FIELD_QUESTION_KEY = "questionKey";
  public static final String FIELD_CREATED = "created";
  public static final String FIELD_ACCEPTED = "accepted";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Parent
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> parentUserKey;

  @Wither
  private String username;

  @Wither
  private Link avatarUrl;

  @Wither
  private String content;

  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<CommentCounterShard>> shardRefs;

  @Index
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Question> questionKey;

  @Wither
  @NonFinal
  @Index(IfNotDefault.class)
  boolean accepted;

  @Index
  @NonFinal
  private DateTime created;

  // Extra fields
  @Ignore
  @Wither
  private Vote.Direction dir;

  @Ignore
  @Wither
  private long votes;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Comment> getKey() {
    return Key.create(parentUserKey, Comment.class, id);
  }

  @JsonProperty("id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @Override
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public <T> Key<T> getVotableKey() {
    //noinspection unchecked
    return (Key<T>) getKey();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public List<CommentCounterShard> getShards() {
    return Deref.deref(this.shardRefs);
  }

  @NoArgsConstructor
  public static final class ShardGroup {
  }
}