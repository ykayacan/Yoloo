package com.yoloo.backend.game.badge;

import com.yoloo.backend.game.Tracker;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "createTracker")
public class PointBadgeRule implements BadgeRule {

  private Tracker tracker;
  private int requiredPoint;

  @Override public boolean isValid() {
    return tracker.getPoints() > requiredPoint;
  }
}
