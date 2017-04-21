package com.yoloo.backend.comment;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.vote.VoteService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class CommentControllerFactory implements ControllerFactory<CommentController> {

  @Override
  public CommentController create() {
    CommentShardService shardService = CommentShardService.create();

    return CommentController.create(
        CommentService.create(shardService),
        shardService,
        PostShardService.create(),
        GameService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()),
        VoteService.create());
  }
}
