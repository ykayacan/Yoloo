package com.yoloo.backend.gamification;

import com.yoloo.backend.gamification.condition.Condition;
import com.yoloo.backend.gamification.reward.Reward;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
public class GameEngine {

  @Singular
  private List<Condition> conditions;

  @Singular
  private List<Tracker> trackers;

  @Singular
  private List<Reward> rewards;
}
