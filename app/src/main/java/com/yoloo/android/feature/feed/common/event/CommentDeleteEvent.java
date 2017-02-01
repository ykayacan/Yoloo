package com.yoloo.android.feature.feed.common.event;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.util.RxBus;

public class CommentDeleteEvent implements RxBus.BusEvent {

  private final PostRealm post;

  public CommentDeleteEvent(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
