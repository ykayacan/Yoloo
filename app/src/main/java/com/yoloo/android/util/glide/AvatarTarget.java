package com.yoloo.android.util.glide;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.ui.widget.AvatarView;

public class AvatarTarget extends SimpleTarget<GlideDrawable> {

  private final AvatarView avatarView;

  public AvatarTarget(AvatarView avatarView) {
    this.avatarView = avatarView;
  }

  @Override public void onResourceReady(GlideDrawable glideDrawable,
      GlideAnimation<? super GlideDrawable> glideAnimation) {
    avatarView.setAvatar(glideDrawable);
  }
}
