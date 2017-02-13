package com.yoloo.backend.game.badge;

import com.google.common.collect.ImmutableList;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MiniSize;
import com.yoloo.backend.media.size.ThumbSize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Badge {

  private String name;
  private String displayName;
  private String badgePhotoUrl;

  public Media getBadgeMedia() {
    ImmutableList<Media.Size> sizes =
        ImmutableList.<Media.Size>builder()
            .add(new ThumbSize(badgePhotoUrl))
            .add(new MiniSize(badgePhotoUrl))
            .add(new LowSize(badgePhotoUrl))
            .build();

    return Media.builder().sizes(sizes).build();
  }
}
