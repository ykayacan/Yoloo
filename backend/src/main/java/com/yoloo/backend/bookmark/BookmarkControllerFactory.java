package com.yoloo.backend.bookmark;

import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.vote.VoteService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class BookmarkControllerFactory implements ControllerFactory<BookmarkController> {

  @Override
  public BookmarkController create() {
    return BookmarkController.create(
        BookmarkService.create(),
        PostShardService.create(),
        VoteService.create());
  }
}
