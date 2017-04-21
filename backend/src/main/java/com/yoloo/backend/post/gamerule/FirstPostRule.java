package com.yoloo.backend.post.gamerule;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.GameConstants;
import com.yoloo.backend.game.Tracker;
import lombok.AllArgsConstructor;
import org.easyrules.annotation.Action;
import org.easyrules.annotation.Condition;
import org.easyrules.annotation.Rule;

@Rule(description = "First post for new user.")
@AllArgsConstructor
public class FirstPostRule {

  private DeviceRecord record;
  private Tracker tracker;

  @Condition public boolean when() {
    return tracker.isFirstPost();
  }

  @Action public void addBounties() throws Exception {
    tracker.addBounties(GameConstants.FIRST_POST.getBounties());
  }

  @Action(order = 1) public void addPoints() throws Exception {
    if (!tracker.isCap()) {
      tracker.addPoints(GameConstants.FIRST_POST.getPoints());
    }
  }
}
