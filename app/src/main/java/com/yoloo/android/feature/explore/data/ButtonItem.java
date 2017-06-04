package com.yoloo.android.feature.explore.data;

import android.support.annotation.NonNull;
import android.widget.Button;
import com.yoloo.android.data.feed.FeedItem;
import javax.annotation.Nonnull;

public class ButtonItem implements FeedItem<Button> {

  @Nonnull @Override public String getId() {
    return ButtonItem.class.getName();
  }

  @NonNull @Override public Button getItem() {
    return (Button) new Object();
  }
}
