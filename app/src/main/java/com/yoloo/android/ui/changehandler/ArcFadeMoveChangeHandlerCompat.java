package com.yoloo.android.ui.changehandler;

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;

public class ArcFadeMoveChangeHandlerCompat extends TransitionChangeHandlerCompat {

  public ArcFadeMoveChangeHandlerCompat() {
    super(new ArcFadeMoveChangeHandler(), new FadeChangeHandler());
  }
}
