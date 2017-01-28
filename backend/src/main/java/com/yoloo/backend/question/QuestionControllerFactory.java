package com.yoloo.backend.question;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.topic.CategoryShardService;
import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class QuestionControllerFactory implements ControllerFactory<QuestionController> {

  @Override
  public QuestionController create() {
    QuestionShardService questionShardService = QuestionShardService.create();
    CommentShardService commentShardService = CommentShardService.create();

    return QuestionController.create(
        QuestionService.create(questionShardService),
        questionShardService,
        CommentService.create(commentShardService),
        commentShardService,
        TagShardService.create(),
        CategoryShardService.create(),
        AccountService.create(),
        AccountShardService.create(),
        GamificationService.create(),
        MediaService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}
