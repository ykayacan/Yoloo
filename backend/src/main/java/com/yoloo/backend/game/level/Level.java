package com.yoloo.backend.game.level;

import com.google.common.collect.Maps;
import ix.Ix;
import java.util.Map;

public class Level {

  private static final Map<Integer, Integer> LEVELS = Maps.newHashMap();

  static {
    LEVELS.put(0, 0);
    LEVELS.put(1, 300);
    LEVELS.put(2, 500);
    LEVELS.put(3, 1000);
    LEVELS.put(4, 2500);
    LEVELS.put(5, 5000);
  }

  public static int findLevelForPoint(int points) {
    return Ix.from(LEVELS.entrySet()).filter(entry -> entry.getValue() <= points).last().getKey();
  }

  public static String findLevelTitleByLevel(int level) {
    switch (level) {
      case 0:
      case 1:
        return "";
      case 2:
        return "Travel Enthusiast";
      case 3:
        return "Explorer";
      case 4:
        return "Traveler";
      case 5:
        return "Full Time Traveler";
      default:
        return "";
    }
  }

  public static int findPointsForLevel(int level) {
    return LEVELS.get(level);
  }
}
