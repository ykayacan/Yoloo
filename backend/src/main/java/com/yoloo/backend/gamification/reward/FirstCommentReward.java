package com.yoloo.backend.gamification.reward;

import com.yoloo.backend.gamification.Level;
import com.yoloo.backend.gamification.Tracker;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class FirstCommentReward implements Reward {

  private Tracker tracker;

  @Override
  public boolean isValid() {
    return !tracker.isFirstComment();
  }

  @Override
  public int getSenderPoints() {
    return 0;
  }

  @Override
  public int getSenderBounties() {
    return 0;
  }

  @Override
  public int getReceiverPoints() {
    return isValid() ? 100 : 0;
  }

  @Override
  public int getReceiverBounties() {
    return 1;
  }

  @Override
  public Tracker getTracker() {
    if (isLevelUp()) {
      tracker.setLevel(tracker.getLevel() + 1);
    }
    tracker.setFirstComment(true);
    return tracker
        .addBounties(getReceiverBounties())
        .addDailyPoints(getReceiverPoints())
        .addPoints(getReceiverPoints());
  }

  @Override
  public boolean isLevelUp() {
    return Level.isLevelUp(tracker, getReceiverPoints());
  }
}
