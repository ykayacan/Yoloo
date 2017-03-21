package com.yoloo.android.ui.changehandler;

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;

public class SharedElementDelayingChangeHandlerCompat extends TransitionChangeHandlerCompat {

  public SharedElementDelayingChangeHandlerCompat() {
    super(new ArcFadeMoveChangeHandler(), new FadeChangeHandler());
  }
}
