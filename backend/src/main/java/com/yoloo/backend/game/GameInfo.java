package com.yoloo.backend.game;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class GameInfo {
  private int points;
  private int requiredPoints;
  private String level;
  private String nextLevel;
  @Singular private List<GameHistory> histories;

  @Value
  @Builder
  public static class GameHistory {
    private int points;
    private int bounties;
  }
}
