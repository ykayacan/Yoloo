package com.yoloo.backend.vote;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Vote {

  public static final String FIELD_VOTABLE_KEY = "votableKey";

  // Websafe voteable id.
  @Id
  private String id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @Wither
  @NonFinal
  private Direction dir;

  @NonFinal
  private Key<Votable> votableKey;

  public static Key<Vote> createKey(Key<? extends Votable> votableKey, Key<Account> accountKey) {
    return Key.create(accountKey, Vote.class, votableKey.toWebSafeString());
  }

  /**
   * Gets key.
   *
   * @return the key
   */
  public Key<Vote> getKey() {
    return Key.create(parent, Vote.class, id);
  }

  /**
   * Is up vote boolean.
   *
   * @return the boolean
   */
  public boolean isUpVote() {
    return this.dir == Direction.UP;
  }

  /**
   * Is down vote boolean.
   *
   * @return the boolean
   */
  public boolean isDownVote() {
    return this.dir == Direction.DOWN;
  }

  /**
   * Is un vote boolean.
   *
   * @return the boolean
   */
  public boolean isDefault() {
    return this.dir == Direction.DEFAULT;
  }

  /**
   * Whether the voter is also the author of the thing voted on.
   *
   * @return boolean boolean
   */
  public boolean isSelfVote() {
    return this.parent.equivalent(votableKey.getParent());
  }

  @AllArgsConstructor
  @Getter
  public enum Direction {
    DEFAULT(0),
    UP(1),
    DOWN(-1);

    private int value;
  }
}