package com.yoloo.backend.gamification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Level {
  LEVEL_0(0, 0),
  LEVEL_1(1, 300),
  LEVEL_2(2, 500),
  LEVEL_3(3, 1000),
  LEVEL_4(4, 2500),
  LEVEL_5(5, 5000);

  int level;

  int requiredPoints;

  public static boolean isLevelUp(Tracker tracker, int earnedPoints) {
    int currentPoints = tracker.getPoints() + earnedPoints;
    for (Level level : Level.values()) {
      if (currentPoints >= level.getRequiredPoints()) {
        return true;
      }
    }
    return false;
  }
}
