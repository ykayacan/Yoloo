package com.yoloo.backend.game;

public enum GameConstants {
  FIRST_POST(120, 2),
  FIRST_COMMENT(0, 0);

  private final int points;
  private final int bounties;

  GameConstants(int points, int bounties) {
    this.points = points;
    this.bounties = bounties;
  }

  public int getPoints() {
    return points;
  }

  public int getBounties() {
    return bounties;
  }
}
