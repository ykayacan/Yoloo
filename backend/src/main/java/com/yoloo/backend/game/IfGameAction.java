package com.yoloo.backend.game;

import com.googlecode.objectify.condition.ValueIf;
import com.yoloo.backend.notification.Action;

public class IfGameAction extends ValueIf<Action> {
  @Override public boolean matchesValue(Action value) {
    return value == Action.GAME;
  }
}
