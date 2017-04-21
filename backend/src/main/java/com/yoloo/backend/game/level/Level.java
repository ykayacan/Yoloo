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

  public static int findPointsForLevel(int level) {
    return LEVELS.get(level);
  }
}
