/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yoloo.android.feature.ui.widget.floatingactionmenu.animation;

import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.animation.Interpolator;

public class AnimationUtils {

  public static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR =
      new FastOutLinearInInterpolator();
  public static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR =
      new LinearOutSlowInInterpolator();

  private static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR
      = () -> new ValueAnimatorCompat(new ValueAnimatorCompatImplHoneycombMr1());

  public static ValueAnimatorCompat createAnimator() {
    return DEFAULT_ANIMATOR_CREATOR.createAnimator();
  }
}
