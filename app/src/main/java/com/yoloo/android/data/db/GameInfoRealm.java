package com.yoloo.android.data.db;

import com.annimon.stream.Stream;
import com.yoloo.backend.yolooApi.model.GameInfo;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.List;
import java.util.Objects;

public class GameInfoRealm extends RealmObject {

  @PrimaryKey private String id;
  private int currentLvlPoints;
  private int currentLvl;

  private int myPoints;
  private String title;

  private int nextLvlPoints;
  private int nextLvl;

  private RealmList<GameHistoryRealm> histories;

  public GameInfoRealm() {
    this.id = "gameInfo";
    this.histories = new RealmList<>();
  }

  public GameInfoRealm(GameInfo info) {
    this();
    this.currentLvl = info.getCurrentLvl();
    this.currentLvlPoints = info.getCurrentLvlPoints();
    this.myPoints = info.getMyPoints();
    this.title = info.getTitle();
    this.nextLvl = info.getNextLvl();
    this.nextLvlPoints = info.getNextLvlPoints();

    if (info.getHistories() != null) {
      Stream
          .of(info.getHistories())
          .map(GameHistoryRealm::new)
          .forEach(gameHistoryRealm -> histories.add(gameHistoryRealm));
    }
  }

  public String getId() {
    return id;
  }

  public int getCurrentLvlPoints() {
    return currentLvlPoints;
  }

  public int getCurrentLvl() {
    return currentLvl;
  }

  public int getMyPoints() {
    return myPoints;
  }

  public String getTitle() {
    return title;
  }

  public int getNextLvlPoints() {
    return nextLvlPoints;
  }

  public int getNextLvl() {
    return nextLvl;
  }

  public List<GameHistoryRealm> getHistories() {
    return histories;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameInfoRealm)) return false;
    GameInfoRealm that = (GameInfoRealm) o;
    return getCurrentLvlPoints() == that.getCurrentLvlPoints()
        && getCurrentLvl() == that.getCurrentLvl()
        && getMyPoints() == that.getMyPoints()
        && getNextLvlPoints() == that.getNextLvlPoints()
        && getNextLvl() == that.getNextLvl()
        && Objects.equals(getId(), that.getId())
        && Objects.equals(getTitle(), that.getTitle())
        && Objects.equals(getHistories(), that.getHistories());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getCurrentLvlPoints(), getCurrentLvl(), getMyPoints(), getTitle(),
        getNextLvlPoints(), getNextLvl(), getHistories());
  }

  @Override
  public String toString() {
    return "GameInfoRealm{"
        + "id='"
        + id
        + '\''
        + ", currentLvlPoints="
        + currentLvlPoints
        + ", currentLvl="
        + currentLvl
        + ", myPoints="
        + myPoints
        + ", title='"
        + title
        + '\''
        + ", nextLvlPoints="
        + nextLvlPoints
        + ", nextLvl="
        + nextLvl
        + ", histories="
        + histories
        + '}';
  }
}
