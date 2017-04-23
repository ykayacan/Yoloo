package com.yoloo.android.data.model;

import com.annimon.stream.Stream;
import com.yoloo.backend.yolooApi.model.GameInfo;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GameInfoRealm extends RealmObject {

  @PrimaryKey private String id;
  private int points;
  private int requiredPoints;
  private String level;
  private String nextLevel;
  private RealmList<GameHistoryRealm> histories;

  public GameInfoRealm() {
    this.id = "gameInfo";
    this.histories = new RealmList<>();
  }

  public GameInfoRealm(GameInfo info) {
    this();
    this.points = info.getPoints();
    this.requiredPoints = info.getRequiredPoints();
    this.level = info.getLevel();
    this.nextLevel = info.getNextLevel();
    if (info.getHistories() != null) {
      Stream
          .of(info.getHistories())
          .map(GameHistoryRealm::new)
          .forEach(gameHistoryRealm -> histories.add(gameHistoryRealm));
    }
  }

  public int getPoints() {
    return points;
  }

  public int getRequiredPoints() {
    return requiredPoints;
  }

  public String getLevel() {
    return level;
  }

  public String getNextLevel() {
    return nextLevel;
  }

  public RealmList<GameHistoryRealm> getHistories() {
    return histories;
  }

  @Override
  public String toString() {
    return "GameInfoRealm{"
        + "id='"
        + id
        + '\''
        + ", points="
        + points
        + ", requiredPoints="
        + requiredPoints
        + ", level='"
        + level
        + '\''
        + ", nextLevel='"
        + nextLevel
        + '\''
        + ", histories="
        + histories
        + '}';
  }
}
