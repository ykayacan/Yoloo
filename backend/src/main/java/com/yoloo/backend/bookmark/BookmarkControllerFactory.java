package com.yoloo.backend.bookmark;

import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class BookmarkControllerFactory implements ControllerFactory<BookmarkController> {

  @Override
  public BookmarkController create() {
    return BookmarkController.create(BookmarkService.create());
  }
}
