package com.yoloo.backend.gamification;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.yoloo.backend.account.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Cache(expirationSeconds = 180)
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tracker {

  public static final int DAILY_CAP = 200;

  /**
   * Account id:tracker
   */
  @Id
  private String id;

  @Index
  private int points;

  @Index
  private int bounties;

  @Index
  private int level;

  private int dailyPoints;

  private boolean firstQuestion;

  private boolean firstComment;

  private boolean firstQuestionOfDay;

  public static Key<Tracker> createKey(Key<Account> accountKey) {
    return Key.create(Tracker.class, accountKey.toWebSafeString() + ":tracker");
  }

  public Key<Tracker> getKey() {
    return Key.create(Tracker.class, this.id);
  }

  public boolean isCap() {
    return this.dailyPoints == DAILY_CAP;
  }

  public Tracker addPoints(int points) {
    this.points += points;
    return this;
  }

  public Tracker addDailyPoints(int points) {
    this.dailyPoints += points;
    return this;
  }

  public Tracker addBounties(int bounties) {
    this.bounties += bounties;
    return this;
  }
}