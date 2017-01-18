package com.yoloo.backend.topic;

import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class TopicControllerFactory implements ControllerFactory<TopicController> {

  @Override
  public TopicController create() {
    return TopicController.create(
        TopicService.create(),
        TopicShardService.create()
    );
  }
}
