package com.yoloo.android.data.db;

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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameHistoryRealm)) return false;

    GameHistoryRealm that = (GameHistoryRealm) o;

    if (getPoints() != that.getPoints()) return false;
    if (getBounties() != that.getBounties()) return false;
    return id.equals(that.id);
  }

  @Override public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + getPoints();
    result = 31 * result + getBounties();
    return result;
  }

  @Override
  public String toString() {
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
