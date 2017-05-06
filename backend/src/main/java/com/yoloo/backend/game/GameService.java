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
  public void addAnswerFirstPostBonus(DeviceRecord record, Tracker tracker,
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
   * Add accept comment bonus post entity.
   *
   * @param postOwnerTracker the post owner tracker
   * @param commenterTracker the commenter tracker
   * @param postOwnerRecord the post owner record
   * @param commentOwnerRecord the comment owner record
   * @param post the post
   * @param postOwnerListener the post owner listener
   * @param commenterListener the commenter listener
   * @return the post entity
   */
  public PostEntity addAcceptCommentBonus(Tracker postOwnerTracker, Tracker commenterTracker,
      DeviceRecord postOwnerRecord, DeviceRecord commentOwnerRecord, PostEntity post,
      NewNotificationListener postOwnerListener, NewNotificationListener commenterListener) {
    if (post.getAcceptedCommentKey() == null) {
      List<Notifiable> postOwnerNotifiables = new ArrayList<>(2);
      List<Notifiable> commenterNotifiables = new ArrayList<>(2);

      final int bonusBounty =
          commenterTracker.getLevel() == 0 ? 1 : commenterTracker.getLevel() + post.getBounty();
      commenterTracker.addBounties(bonusBounty);

      boolean updatedPostOwnerLevel = false;
      int postOwnerPoints = 0;
      if (!postOwnerTracker.isCap()) {
        postOwnerTracker.addPoints(20);
        postOwnerPoints = 20;
        updatedPostOwnerLevel = updateLevel(postOwnerTracker);
      }

      boolean updateCommenterLevel = false;
      int commenterPoints = 0;
      if (!commenterTracker.isCap()) {
        commenterTracker.addPoints(50);
        commenterPoints = 50;
        updateCommenterLevel = updateLevel(commenterTracker);
      }

      postOwnerNotifiables.add(GameBonusNotifiable.create(postOwnerRecord, postOwnerPoints, 0));
      commenterNotifiables.add(
          GameBonusNotifiable.create(commentOwnerRecord, commenterPoints, bonusBounty));

      if (updatedPostOwnerLevel) {
        postOwnerNotifiables.add(LevelUpNotifiable.create(postOwnerRecord, postOwnerTracker));
        postOwnerListener.newNotifications(postOwnerNotifiables);
      }
      if (updateCommenterLevel) {
        commenterNotifiables.add(LevelUpNotifiable.create(commentOwnerRecord, commenterTracker));
        commenterListener.newNotifications(commenterNotifiables);
      }

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