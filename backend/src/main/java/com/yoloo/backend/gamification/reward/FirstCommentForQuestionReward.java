package com.yoloo.backend.gamification.reward;

import com.yoloo.backend.gamification.Level;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.question.Question;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class FirstCommentForQuestionReward implements Reward {

  private Tracker tracker;
  private Question question;

  @Override
  public boolean isValid() {
    return !tracker.isCap() && !question.isCommented();
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
    return isValid() ? 20 : 0;
  }

  @Override
  public int getReceiverBounties() {
    return 0;
  }

  @Override
  public Tracker getTracker() {
    if (isLevelUp()) {
      tracker.setLevel(tracker.getLevel() + 1);
    }
    return tracker.addBounties(getReceiverBounties())
        .addDailyPoints(getReceiverPoints())
        .addPoints(getReceiverPoints());
  }

  @Override
  public boolean isLevelUp() {
    return Level.isLevelUp(tracker, getReceiverPoints());
  }
}
