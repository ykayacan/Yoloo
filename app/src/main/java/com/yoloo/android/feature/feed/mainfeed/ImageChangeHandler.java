package com.yoloo.android.feature.feed.mainfeed;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler;
import com.yoloo.android.R;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ImageChangeHandler extends TransitionChangeHandler {

  private static final String KEY_TRANSITION_NAME = "TRANSITION_NAME";

  @NonNull
  @Override
  protected Transition getTransition(@NonNull ViewGroup container, @Nullable View from,
      @Nullable View to, boolean isPush) {
    TransitionSet transition = new TransitionSet()
        .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
        .addTransition(new Fade(Fade.OUT))
        .addTransition(new TransitionSet().addTransition(new ChangeBounds())
            .addTransition(new ChangeClipBounds())
            .addTransition(new ChangeTransform()))
        .addTransition(new Fade(Fade.IN))
        .addTarget(container.getResources().getString(R.string.transition_content_photo));

    /*TransitionSet transition = new TransitionSet()
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .addTransition(new ChangeBounds())
        .addTransition(new ChangeTransform())
        .addTransition(new ChangeImageTransform())
        .addTarget(container.getResources().getString(R.string.transition_content_photo));*/

    transition.setPathMotion(new ArcMotion());

    return transition;
  }
}
