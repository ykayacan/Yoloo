package com.yoloo.backend.vote;

import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class VoteControllerFactory implements ControllerFactory<VoteController> {

  @Override
  public VoteController create() {
    return VoteController.create(
        QuestionShardService.create(),
        CommentShardService.create()
    );
  }
}
