package com.yoloo.backend.gamification.reward;

import com.yoloo.backend.gamification.Tracker;

public interface Reward {

  boolean isValid();

  int getSenderPoints();

  int getSenderBounties();

  int getReceiverPoints();

  int getReceiverBounties();

  Tracker getTracker();

  boolean isLevelUp();
}
