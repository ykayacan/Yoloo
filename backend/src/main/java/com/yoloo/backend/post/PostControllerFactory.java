package com.yoloo.backend.post;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.tag.TagService;
import com.yoloo.backend.vote.VoteService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class PostControllerFactory implements ControllerFactory<PostController> {

  @Override
  public PostController create() {
    CommentShardService commentShardService = CommentShardService.create();
    PostShardService postShardService = PostShardService.create();

    return PostController.create(PostService.create(postShardService),
        postShardService,
        CommentService.create(commentShardService),
        commentShardService,
        AccountShardService.create(),
        GameService.create(),
        MediaService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()),
        VoteService.create(),
        new TagService());
  }
}
