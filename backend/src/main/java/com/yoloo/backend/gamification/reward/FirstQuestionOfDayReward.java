package com.yoloo.backend.gamification.reward;

import com.yoloo.backend.gamification.Level;
import com.yoloo.backend.gamification.Tracker;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class FirstQuestionOfDayReward implements Reward {

  private Tracker tracker;

  @Override
  public boolean isValid() {
    return !tracker.isCap() && !tracker.isFirstQuestionOfDay();
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
    return 20;
  }

  @Override
  public int getReceiverBounties() {
    return tracker.getLevel() == 0 ? 1 : tracker.getLevel();
  }

  @Override
  public Tracker getTracker() {
    if (isLevelUp()) {
      tracker.setLevel(tracker.getLevel() + 1);
    }
    tracker.setFirstQuestionOfDay(true);
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
