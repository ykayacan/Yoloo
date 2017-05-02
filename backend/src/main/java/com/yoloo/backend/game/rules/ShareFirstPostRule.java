package com.yoloo.backend.game.rules;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.type.GameBonusNotifiable;
import com.yoloo.backend.notification.type.LevelUpNotifiable;
import com.yoloo.backend.notification.type.Notifiable;
import java.util.List;
import lombok.Value;
import org.easyrules.annotation.Action;
import org.easyrules.annotation.Condition;
import org.easyrules.annotation.Rule;
import org.joda.time.DateTime;

@Value
@Rule(name = "Share 1st post for given user")
public class ShareFirstPostRule {

  public static final int POINTS = 100;
  public static final int BOUNTIES = 1;

  private Tracker tracker;
  private DeviceRecord record;
  private List<Notifiable> bundles;

  @Condition
  public boolean when() {
    return !tracker.isFirstPost();
  }

  @Action
  public void setupDefault() {
    tracker.setFirstPost(true);
    tracker.setPostBonusAwardedAt(DateTime.now());
  }

  @Action(order = 1)
  public void thenAddPoints() {
    tracker.addBounties(BOUNTIES);

    int points = 0;
    if (!tracker.isCap()) {
      points = POINTS;
      tracker.addPoints(POINTS);
    }

    bundles.add(GameBonusNotifiable.create(record, points, BOUNTIES));

    if (tracker.checkLevelUp()) {
      bundles.add(LevelUpNotifiable.create(record, tracker));
    }
  }
}
