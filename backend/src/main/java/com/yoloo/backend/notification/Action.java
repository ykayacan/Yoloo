package com.yoloo.backend.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {
  FOLLOW(1),
  COMMENT(2),
  MENTION(3),
  GAME(4),
  ACCEPT(5),
  FOLLOWER_POST(6),
  USER_JOINED(7),
  GROUP_POST(8);

  int value;

  public String getValueString() {
    return String.valueOf(value);
  }

  @AllArgsConstructor
  @Getter
  public enum GameAction {
    LEVEL_UP(1), BONUS(2);

    int value;

    public String getValueString() {
      return String.valueOf(value);
    }
  }
}
