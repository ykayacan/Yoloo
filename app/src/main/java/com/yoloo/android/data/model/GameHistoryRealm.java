package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.GameHistory;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GameHistoryRealm extends RealmObject {
  @PrimaryKey private String id;
  private int points;
  private int bounties;

  public GameHistoryRealm() {
    this.id = "gameHistory";
  }

  public GameHistoryRealm(GameHistory history) {
    this();
    this.points = history.getPoints();
    this.bounties = history.getBounties();
  }

  public int getPoints() {
    return points;
  }

  public int getBounties() {
    return bounties;
  }

  @Override public String toString() {
    return "GameHistoryRealm{"
        + "id='"
        + id
        + '\''
        + ", points="
        + points
        + ", bounties="
        + bounties
        + '}';
  }
}
