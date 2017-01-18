package com.yoloo.backend.gamification.reward;

import com.google.common.base.Optional;
import com.yoloo.backend.gamification.Level;
import com.yoloo.backend.gamification.Tracker;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class AcceptCommentReward implements Reward {

  private Tracker asker;
  private Tracker answerer;
  private Optional<Boolean> accepted;

  @Override
  public boolean isValid() {
    return !asker.isCap() && accepted.isPresent();
  }

  @Override
  public int getSenderPoints() {
    return 20;
  }

  @Override
  public int getSenderBounties() {
    return 0;
  }

  @Override
  public int getReceiverPoints() {
    return 50;
  }

  @Override
  public int getReceiverBounties() {
    return answerer.getLevel() == 0 ? 1 : answerer.getLevel();
  }

  @Override
  public Tracker getTracker() {
    if (isLevelUp()) {
      answerer.setLevel(answerer.getLevel() + 1);
    }
    return answerer
        .addBounties(getReceiverBounties())
        .addDailyPoints(getReceiverPoints())
        .addPoints(getReceiverPoints());
  }

  @Override
  public boolean isLevelUp() {
    return Level.isLevelUp(asker, getSenderPoints());
  }

  public boolean isLevelUpReceiver() {
    return Level.isLevelUp(answerer, getReceiverPoints());
  }
}
