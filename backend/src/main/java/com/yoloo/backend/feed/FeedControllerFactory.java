package com.yoloo.backend.feed;

import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class FeedControllerFactory implements ControllerFactory<FeedController> {

  @Override
  public FeedController create() {
    return FeedController.create(FeedService.create());
  }
}
