package com.yoloo.backend.vote;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
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

  /**
   * The constant FIELD_VOTABLE_KEY.
   */
  public static final String FIELD_VOTABLE_KEY = "votableKey";

  // Websafe voteable id.
  @Id
  private String id;

  @Parent
  private Key<Account> parentUserKey;

  @Wither
  private Direction dir;

  @Index
  @NonFinal
  private Key<Votable> votableKey;

  /**
   * Create key key.
   *
   * @param questionKey the question key
   * @param accountKey the account key
   * @return the key
   */
  public static Key<Vote> createKey(Key<Question> questionKey, Key<Account> accountKey) {
    return Key.create(accountKey, Vote.class, questionKey.toWebSafeString());
  }

  /**
   * Gets key.
   *
   * @return the key
   */
  public Key<Vote> getKey() {
    return Key.create(parentUserKey, Vote.class, id);
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
    return this.parentUserKey.equivalent(votableKey.getParent());
  }

  /**
   * The enum Direction.
   */
  @AllArgsConstructor
  @Getter
  public enum Direction {
    /**
     * Unvote direction.
     */
    DEFAULT(0), /**
     * Up direction.
     */
    UP(1), /**
     * Down direction.
     */
    DOWN(-1);

    private int value;
  }
}