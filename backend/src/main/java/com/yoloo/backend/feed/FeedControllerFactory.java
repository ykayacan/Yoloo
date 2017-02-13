package com.yoloo.backend.feed;

import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.vote.VoteService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class FeedControllerFactory implements ControllerFactory<FeedController> {

  @Override
  public FeedController create() {
    return FeedController.create(PostShardService.create(), VoteService.create());
  }
}
