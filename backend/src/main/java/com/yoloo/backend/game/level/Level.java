package com.yoloo.backend.game.level;

import com.google.common.collect.Maps;
import java.util.NavigableMap;

public class Level {

  private static final NavigableMap<Integer, Integer> LEVELS = Maps.newTreeMap();

  static {
    LEVELS.put(0, 0);
    LEVELS.put(300, 1);
    LEVELS.put(500, 2);
    LEVELS.put(1000, 3);
    LEVELS.put(2500, 4);
    LEVELS.put(5000, 5);
  }

  public static int findLevelForPoint(int points) {
    return LEVELS.floorEntry(points).getValue();
  }
}
