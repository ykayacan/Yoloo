package com.yoloo.android.feature.feed.common.event;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.util.RxBus;

public final class DeleteEvent implements RxBus.BusEvent {

  private final PostRealm post;

  public DeleteEvent(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
