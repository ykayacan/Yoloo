package com.yoloo.backend.gamification.reward;

import com.yoloo.backend.gamification.Tracker;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class FirstQuestionReward implements Reward {

  private Tracker tracker;

  @Override
  public boolean isValid() {
    return !tracker.isFirstQuestion();
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
    return 100;
  }

  @Override
  public int getReceiverBounties() {
    return 1;
  }

  @Override
  public Tracker getTracker() {
    return null;
  }

  @Override
  public boolean isLevelUp() {
    return false;
  }
}
