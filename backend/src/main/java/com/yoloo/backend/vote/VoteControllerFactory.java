package com.yoloo.backend.vote;

import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.post.PostShardService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class VoteControllerFactory implements ControllerFactory<VoteController> {

  @Override
  public VoteController create() {
    return VoteController.create(
        PostShardService.create(),
        CommentShardService.create(),
        VoteService.create()
    );
  }
}
