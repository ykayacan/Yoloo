package com.yoloo.backend.game;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotZero;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.game.badge.Badge;
import com.yoloo.backend.game.level.Level;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.joda.time.DateTime;

@Entity
@Cache(expirationSeconds = 180)
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tracker {

  private static final int DAILY_CAP = 200;

  /**
   * tracker:accountId
   */
  @Id private String id;

  @Index(IfNotZero.class) private int points;

  @Index(IfNotZero.class) private int bounties;

  @Index private int level;

  private String title;

  private int dailyPoints;

  private boolean firstPost;

  private boolean firstComment;

  private DateTime postBonusAwardedAt;

  @Singular private Set<Badge> badges;

  public static Key<Tracker> createKey(Key<Account> accountKey) {
    return Key.create(Tracker.class, "tracker:" + accountKey.toWebSafeString());
  }

  public Key<Tracker> getKey() {
    return Key.create(Tracker.class, id);
  }

  public boolean hasBadge(Badge badge) {
    return badges.contains(badge);
  }

  public boolean isCap() {
    return dailyPoints == DAILY_CAP;
  }

  public Tracker addPoints(int points) {
    this.points += points;
    this.dailyPoints += points;
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

  public void addBadge(Badge badge) {
    badges.add(badge);
  }

  public boolean hasEnoughBounty(int toConsume) {
    return bounties >= toConsume;
  }

  public boolean checkLevelUp() {
    if (isLevelUp(points)) {
      level++;
      return true;
    }

    return false;
  }

  private boolean isLevelUp(int points) {
    return Level.findLevelForPoint(points) > level;
  }
}