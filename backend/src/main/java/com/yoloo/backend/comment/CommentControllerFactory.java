package com.yoloo.backend.comment;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class CommentControllerFactory implements ControllerFactory<CommentController> {

  @Override
  public CommentController create() {
    CommentShardService shardService = CommentShardService.create();

    return CommentController.create(
        CommentService.create(shardService),
        shardService,
        QuestionShardService.create(),
        GamificationService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}
