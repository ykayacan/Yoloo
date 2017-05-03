package com.yoloo.backend.game;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.badge.Badge;
import com.yoloo.backend.game.badge.BadgeRule;
import com.yoloo.backend.game.level.Level;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.type.GameBonusNotifiable;
import com.yoloo.backend.notification.type.LevelUpNotifiable;
import com.yoloo.backend.notification.type.Notifiable;
import com.yoloo.backend.post.PostEntity;
import ix.Ix;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import static com.yoloo.backend.OfyService.ofy;

public class GameService {

  private GameService() {
  }

  public static GameService create() {
    return new GameService();
  }

  public Tracker createTracker(Key<Account> accountKey) {
    return Tracker
        .builder()
        .id(Tracker.createKey(accountKey).getName())
        .level(0)
        .bounties(0)
        .points(0)
        .dailyPoints(0)
        .firstComment(false)
        .firstPost(false)
        .build();
  }

  public GameInfo getGameInfo(User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());
    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);

    Tracker tracker = ofy().load().key(trackerKey).now();

    List<Notification> notifications = ofy()
        .load()
        .type(Notification.class)
        .ancestor(accountKey)
        .filter(Notification.FIELD_ACTION + " =", Action.GAME)
        .order("-" + Notification.FIELD_CREATED)
        .limit(15)
        .list();

    List<GameInfo.GameHistory> histories = Ix.from(notifications).map(notification -> {
      Map<String, Object> payload = notification.getPayloads();
      final long points = (long) payload.get("points");
      final long bounties = (long) payload.get("bounties");

      return GameInfo.GameHistory.builder().points((int) points).bounties((int) bounties).build();
    }).toList();

    final int currentLvl = tracker.getLevel();
    final int currentLvlPoints = Level.findPointsForLevel(currentLvl);

    final int myPoints = tracker.getPoints();

    final int nextLvl = currentLvl + 1;
    final int nextLvlPoints = Level.findPointsForLevel(nextLvl);

    return GameInfo
        .builder()
        .currentLvl(currentLvl)
        .currentLvlPoints(currentLvlPoints)
        .myPoints(myPoints)
        .title(tracker.getTitle())
        .nextLvl(nextLvl)
        .nextLvlPoints(nextLvlPoints)
        .histories(histories)
        .build();
  }

  /**
   * Add first question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addShareFirstPostBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {

    if (!tracker.isFirstPost()) {
      List<Notifiable> bundles = new ArrayList<>(2);

      tracker.addBounties(1);
      tracker.setFirstPost(true);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        points = 100;
        tracker.addPoints(points);
        tracker.setPostBonusAwardedAt(DateTime.now());
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotifiable.create(record, points, 1));
      if (levelUpdated) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
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
  public void addAnswerFirstQuestionBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {

    if (!tracker.isFirstComment()) {
      List<Notifiable> bundles = new ArrayList<>(2);

      tracker.addBounties(1);
      tracker.setFirstComment(true);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        points = 100;
        tracker.addPoints(points);
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotifiable.create(record, points, 1));
      if (levelUpdated) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
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
  public void addSharePostPerDayBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    final boolean isOneDayPassed =
        Days.daysBetween(DateTime.now(), tracker.getPostBonusAwardedAt()).getDays() == 1;

    if (isOneDayPassed) {
      List<Notifiable> bundles = new ArrayList<>(2);

      final int bonusBounty = tracker.getLevel() == 0 ? 1 : tracker.getLevel();
      tracker.addBounties(bonusBounty);

      boolean levelUpdated = false;
      int points = 0;

      if (!tracker.isCap()) {
        points = 20;
        tracker.addPoints(points);
        levelUpdated = updateLevel(tracker);
      }

      bundles.add(GameBonusNotifiable.create(record, points, bonusBounty));
      if (levelUpdated) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add first answerer per day bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param postEntity the post
   * @param listener the listener
   */
  public void addFirstCommenterBonus(DeviceRecord record, Tracker tracker, PostEntity postEntity,
      NewNotificationListener listener) {
    if (!tracker.isCap() && !postEntity.isCommented()) {
      List<Notifiable> bundles = new ArrayList<>(2);

      tracker.addPoints(10);

      bundles.add(GameBonusNotifiable.create(record, 10, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
      }

      listener.newNotifications(bundles);
    }
  }

  /**
   * Add answer to unanswered question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param postEntity the post
   * @param listener the listener
   */
  public void addAnswerToUnansweredQuestionBonus(DeviceRecord record, Tracker tracker,
      PostEntity postEntity, NewNotificationListener listener) {
    final boolean isOneHourPassed =
        Hours.hoursBetween(DateTime.now(), postEntity.getCreated()).getHours() == 1;

    if (!tracker.isCap() && isOneHourPassed) {
      List<Notifiable> bundles = new ArrayList<>(2);

      tracker.addPoints(20);

      bundles.add(GameBonusNotifiable.create(record, 20, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
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
   * @param postEntity the post
   * @param askerListener the asker listener
   * @param answererListener the answerer listener
   * @return the post
   */
  public PostEntity addAcceptCommentBonus(Tracker askerTracker, Tracker answererTracker,
      DeviceRecord askerRecord, DeviceRecord answererRecord, PostEntity postEntity,
      NewNotificationListener askerListener, NewNotificationListener answererListener) {
    if (postEntity.getAcceptedCommentId() == null) {
      List<Notifiable> askerBundle = new ArrayList<>(2);
      List<Notifiable> answererBundle = new ArrayList<>(2);

      final int bonusBounty =
          answererTracker.getLevel() == 0 ? 1 : answererTracker.getLevel() + postEntity.getBounty();
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

      askerBundle.add(GameBonusNotifiable.create(askerRecord, askerPoints, 0));
      answererBundle.add(GameBonusNotifiable.create(answererRecord, answererPoints, bonusBounty));

      if (updatedAskerLevel) {
        askerBundle.add(LevelUpNotifiable.create(askerRecord, askerTracker));
      }
      if (updateAnswererLevel) {
        answererBundle.add(LevelUpNotifiable.create(answererRecord, answererTracker));
      }

      askerListener.newNotifications(askerBundle);
      answererListener.newNotifications(answererBundle);

      return postEntity.withBounty(0);
    }

    return postEntity;
  }

  /**
   * Add share question bonus.
   *
   * @param record the record
   * @param tracker the tracker
   * @param listener the listener
   */
  public void addSharePostBonus(DeviceRecord record, Tracker tracker,
      NewNotificationListener listener) {
    if (!tracker.isCap()) {
      List<Notifiable> bundles = new ArrayList<>(2);

      tracker.addPoints(5);

      bundles.add(GameBonusNotifiable.create(record, 20, 0));
      if (updateLevel(tracker)) {
        bundles.add(LevelUpNotifiable.create(record, tracker));
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
    List<Notifiable> bundles = new ArrayList<>(2);

    tracker.addBounties(2);

    boolean levelUpdated = false;
    int points = 0;

    if (!tracker.isCap()) {
      points = 10;
      tracker.addPoints(points);
      levelUpdated = updateLevel(tracker);
    }

    bundles.add(GameBonusNotifiable.create(record, points, 2));
    if (levelUpdated) {
      bundles.add(LevelUpNotifiable.create(record, tracker));
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
    List<Notifiable> bundles = new ArrayList<>(2);

    tracker.addBounties(2);

    boolean levelUpdated = false;
    int points = 0;

    if (!tracker.isCap()) {
      points = 20;
      tracker.addPoints(points);
      levelUpdated = updateLevel(tracker);
    }

    bundles.add(GameBonusNotifiable.create(record, points, 10));
    if (levelUpdated) {
      bundles.add(LevelUpNotifiable.create(record, tracker));
    }

    listener.newNotifications(bundles);
  }

  private boolean isLevelUp(Tracker tracker, int points) {
    return Level.findLevelForPoint(points) > tracker.getLevel();
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
    void newNotifications(List<Notifiable> bundles);
  }
}