package com.yoloo.backend.game;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.badge.Badge;
import com.yoloo.backend.game.badge.BadgeRule;
import com.yoloo.backend.notification.type.GameBonusNotification;
import com.yoloo.backend.notification.type.LevelUpNotification;
import com.yoloo.backend.notification.type.NotificationBundle;
import com.yoloo.backend.post.Post;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import static com.yoloo.backend.game.level.Level.findLevelForPoint;

@RequiredArgsConstructor(staticName = "create")
public class GamificationService {

  private static final Logger LOG =
      Logger.getLogger(GamificationService.class.getName());

  public Tracker createTracker(Key<Account> accountKey) {
    return Tracker.builder()
        .id(accountKey.toWebSafeString() + ":tracker")
        .level(0)
        .bounties(0)
        .points(0)
        .dailyPoints(0)
        .firstComment(false)
        .firstQuestion(false)
        .build();
  }

  /**
   * Add first question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addFirstQuestionBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {

    if (!tracker.isFirstQuestion()) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      tracker.addBounties(2);
      tracker.setFirstQuestion(true);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        tracker.addPoints(120);
        tracker.setQuestionBonusAwardedAt(DateTime.now());
        points = 120;
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotification.create(record, points, 2));
      if (levelUpdated) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add first answer bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addFirstAnswerBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {

    if (!tracker.isFirstComment()) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      tracker.addBounties(1);
      tracker.setFirstComment(true);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        tracker.addPoints(100);
        points = 100;
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotification.create(record, points, 1));
      if (levelUpdated) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add ask question per day bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addAskQuestionPerDayBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    final boolean isOneDayPassed =
        Days.daysBetween(DateTime.now(), tracker.getQuestionBonusAwardedAt()).getDays() == 1;

    if (isOneDayPassed) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      final int bonusBounty = tracker.getLevel() == 0 ? 1 : tracker.getLevel();
      tracker.addBounties(bonusBounty);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        tracker.addPoints(20);
        points = 20;
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotification.create(record, points, bonusBounty));
      if (levelUpdated) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add first answerer per day bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param post the post
   * @param listener the listener
   */
  public void addFirstAnswererPerDayBonus(DeviceRecord record, Tracker tracker, Post post,
      NewNotificationListener listener) {
    if (!tracker.isCap() && !post.isCommented()) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      tracker.addPoints(20);

      bundles.add(GameBonusNotification.create(record, 20, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add answer to unanswered question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param post the post
   * @param listener the listener
   */
  public void addAnswerToUnansweredQuestionBonus(DeviceRecord record, Tracker tracker, Post post,
      NewNotificationListener listener) {
    final boolean isOneHourPassed =
        Hours.hoursBetween(DateTime.now(), post.getCreated()).getHours() == 1;

    if (!tracker.isCap() && isOneHourPassed) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      tracker.addPoints(10);

      bundles.add(GameBonusNotification.create(record, 10, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add accept comment bonus post.
   *
   * @param askerTracker the asker tracker
   * @param answererTracker the answerer tracker
   * @param askerRecord the asker record
   * @param answererRecord the answerer record
   * @param post the post
   * @param askerListener the asker listener
   * @param answererListener the answerer listener
   * @return the post
   */
  public Post addAcceptCommentBonus(Tracker askerTracker, Tracker answererTracker,
      DeviceRecord askerRecord, DeviceRecord answererRecord, Post post,
      NewNotificationListener askerListener, NewNotificationListener answererListener) {
    if (post.getAcceptedCommentId() == null) {
      List<NotificationBundle> askerBundle = new ArrayList<>(2);
      List<NotificationBundle> answererBundle = new ArrayList<>(2);

      final int bonusBounty =
          answererTracker.getLevel() == 0 ? 1 : answererTracker.getLevel() + post.getBounty();
      answererTracker.addBounties(bonusBounty);

      boolean updatedAskerLevel = false;
      int askerPoints = 0;
      if (!askerTracker.isCap()) {
        askerTracker.addPoints(20);
        askerPoints = 20;
        updatedAskerLevel = updateLevel(askerTracker);
      }

      boolean updateAnswererLevel = false;
      int answererPoints = 0;
      if (!answererTracker.isCap()) {
        answererTracker.addPoints(50);
        answererPoints = 50;
        updateAnswererLevel = updateLevel(answererTracker);
      }

      askerBundle.add(GameBonusNotification.create(askerRecord, askerPoints, 0));
      answererBundle.add(GameBonusNotification.create(answererRecord, answererPoints, bonusBounty));

      if (updatedAskerLevel) {
        askerBundle.add(LevelUpNotification.create(askerRecord, askerTracker));
      }
      if (updateAnswererLevel) {
        answererBundle.add(LevelUpNotification.create(answererRecord, answererTracker));
      }

      askerListener.newNotifications(askerBundle);
      answererListener.newNotifications(answererBundle);

      return post.withBounty(0);
    }

    return post;
  }

  /**
   * Add share question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addShareQuestionBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    if (!tracker.isCap()) {
      List<NotificationBundle> bundles = new ArrayList<>(2);

      tracker.addPoints(5);

      bundles.add(GameBonusNotification.create(record, 20, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotification.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add invite friends bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addInviteFriendsBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    List<NotificationBundle> bundles = new ArrayList<>(2);

    tracker.addBounties(2);

    boolean levelUpdated = false;
    int points = 0;

    if (!tracker.isCap()) {
      tracker.addPoints(10);
      points = 10;
      levelUpdated = updateLevel(tracker);
    }

    bundles.add(GameBonusNotification.create(record, points, 2));
    if (levelUpdated) {
      bundles.add(LevelUpNotification.create(record, tracker));
    }

    listener.newNotifications(bundles);
  }

  /**
   * Add rate app bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addRateAppBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    List<NotificationBundle> bundles = new ArrayList<>(2);

    tracker.addBounties(2);

    boolean levelUpdated = false;
    int points = 0;

    if (!tracker.isCap()) {
      tracker.addPoints(20);
      points = 20;
      levelUpdated = updateLevel(tracker);
    }

    bundles.add(GameBonusNotification.create(record, points, 2));
    if (levelUpdated) {
      bundles.add(LevelUpNotification.create(record, tracker));
    }

    listener.newNotifications(bundles);
  }

  private boolean isLevelUp(Tracker tracker, int points) {
    return findLevelForPoint(points) > tracker.getLevel();
  }

  public void awardBadge(Tracker tracker, Badge badge, Set<BadgeRule> rules) {
    if (!tracker.hasBadge(badge) && isRulesAreValid(rules)) {
      tracker.addBadge(badge);
    }
  }

  private boolean isRulesAreValid(Set<BadgeRule> rules) {
    boolean rulesValid = false;

    for (BadgeRule rule : rules) {
      if (rule.isValid()) {
        rulesValid = true;
      } else {
        rulesValid = false;
        break;
      }
    }
    return rulesValid;
  }

  private boolean updateLevel(Tracker tracker) {
    if (isLevelUp(tracker, tracker.getPoints())) {
      tracker.setLevel(tracker.getLevel() + 1);
      return true;
    }

    return false;
  }

  public interface NewNotificationListener {
    void newNotifications(List<NotificationBundle> bundles);
  }
}