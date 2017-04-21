/*
 * Copyright 2015 Google Inc.
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

package com.yoloo.android.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Property;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import com.yoloo.android.ui.widget.RevealFrameLayout;
import java.util.ArrayList;

/**
 * Utility methods for working with animations.
 */
public final class AnimUtils {

  private static Interpolator fastOutSlowIn;
  private static Interpolator fastOutLinearIn;
  private static Interpolator linearOutSlowIn;
  private static Interpolator decelerate;
  private static Interpolator linear;
  private static Interpolator overshoot;
  private static Interpolator anticipate;
  private static Interpolator accelerateDecelerate;

  private static ArgbEvaluator argbEvaluator;

  private AnimUtils() {
    // empty constructor
  }

  public static Interpolator getLinearInterpolator() {
    if (linear == null) {
      linear = new LinearInterpolator();
    }
    return linear;
  }

  public static Interpolator getFastOutSlowInInterpolator() {
    if (fastOutSlowIn == null) {
      fastOutSlowIn = new FastOutSlowInInterpolator();
    }
    return fastOutSlowIn;
  }

  public static Interpolator getFastOutLinearInInterpolator() {
    if (fastOutLinearIn == null) {
      fastOutLinearIn = new FastOutLinearInInterpolator();
    }
    return fastOutLinearIn;
  }

  public static Interpolator getLinearOutSlowInInterpolator() {
    if (linearOutSlowIn == null) {
      linearOutSlowIn = new LinearOutSlowInInterpolator();
    }
    return linearOutSlowIn;
  }

  public static Interpolator getDecelerateInterpolator() {
    if (decelerate == null) {
      decelerate = new DecelerateInterpolator();
    }
    return decelerate;
  }

  public static Interpolator getOvershootInterpolator() {
    if (overshoot == null) {
      overshoot = new OvershootInterpolator();
    }
    return overshoot;
  }

  public static Interpolator getAnticipateInterpolator() {
    if (anticipate == null) {
      anticipate = new AnticipateInterpolator();
    }
    return anticipate;
  }

  public static Interpolator getAccelerateDecelerateInterpolator() {
    if (accelerateDecelerate == null) {
      accelerateDecelerate = new AccelerateDecelerateInterpolator();
    }
    return accelerateDecelerate;
  }

  public static ArgbEvaluator getArgbEvaluator() {
    if (argbEvaluator == null) {
      argbEvaluator = new ArgbEvaluator();
    }
    return argbEvaluator;
  }

  /**
   * Linear interpolate between a and b with parameter t.
   */
  public static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }

  public static ValueAnimator ofArgb(int... values) {
    if (VersionUtil.hasL()) {
      return ValueAnimator.ofArgb(values);
    } else {
      ValueAnimator anim = new ValueAnimator();
      anim.setIntValues(values);
      anim.setEvaluator(AnimUtils.getArgbEvaluator());
      return anim;
    }
  }

  /**
   * The animation framework has an optimization for <code>Properties</code> from type
   * <code>int</code> but it was only made public in API24, so wrap the impl in our own type
   * and conditionally create the appropriate type, delegating the implementation.
   */
  public static <T> Property<T, Integer> createIntProperty(final IntProp<T> impl) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return new IntProperty<T>(impl.name) {
        @Override
        public Integer get(T object) {
          return impl.get(object);
        }

        @Override
        public void setValue(T object, int value) {
          impl.set(object, value);
        }
      };
    } else {
      return new Property<T, Integer>(Integer.class, impl.name) {
        @Override
        public Integer get(T object) {
          return impl.get(object);
        }

        @Override
        public void set(T object, Integer value) {
          impl.set(object, value);
        }
      };
    }
  }

  /**
   * The animation framework has an optimization for <code>Properties</code> from type
   * <code>float</code> but it was only made public in API24, so wrap the impl in our own type
   * and conditionally create the appropriate type, delegating the implementation.
   */
  public static <T> Property<T, Float> createFloatProperty(final FloatProp<T> impl) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return new FloatProperty<T>(impl.name) {
        @Override
        public Float get(T object) {
          return impl.get(object);
        }

        @Override
        public void setValue(T object, float value) {
          impl.set(object, value);
        }
      };
    } else {
      return new Property<T, Float>(Float.class, impl.name) {
        @Override
        public Float get(T object) {
          return impl.get(object);
        }

        @Override
        public void set(T object, Float value) {
          impl.set(object, value);
        }
      };
    }
  }

  public static Animator createCircularReveal(RevealFrameLayout view, int x, int y,
      float startRadius, float endRadius) {
    if (VersionUtil.hasL()) {
      return ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
    } else {
      view.setClipOutLines(true);
      view.setClipCenter(x, y);
      final Animator reveal = ObjectAnimator.ofFloat(view, "ClipRadius", startRadius, endRadius);
      reveal.addListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          view.setClipOutLines(false);
        }
      });

      return reveal;
    }
  }

  /**
   * A delegate for creating a {@link Property} from <code>int</code> type.
   */
  public abstract static class IntProp<T> {
    public final String name;

    public IntProp(String name) {
      this.name = name;
    }

    public abstract void set(T object, int value);

    public abstract int get(T object);
  }

  /**
   * A delegate for creating a {@link Property} from <code>float</code> type.
   */
  public abstract static class FloatProp<T> {
    public final String name;

    protected FloatProp(String name) {
      this.name = name;
    }

    public abstract void set(T object, float value);

    public abstract float get(T object);
  }

  /**
   * https://halfthought.wordpress.com/2014/11/07/reveal-transition/
   * <p/>
   * Interrupting Activity transitions can yield an OperationNotSupportedException when the
   * transition tries to pause the animator. Yikes! We can fix this by wrapping the Animator:
   */
  public static class NoPauseAnimator extends Animator {
    private final Animator mAnimator;
    private final ArrayMap<AnimatorListener, AnimatorListener> mListeners = new ArrayMap<>();

    public NoPauseAnimator(Animator animator) {
      mAnimator = animator;
    }

    @Override
    public void addListener(AnimatorListener listener) {
      AnimatorListener wrapper = new AnimatorListenerWrapper(this, listener);
      if (!mListeners.containsKey(listener)) {
        mListeners.put(listener, wrapper);
        mAnimator.addListener(wrapper);
      }
    }

    @Override
    public void cancel() {
      mAnimator.cancel();
    }

    @Override
    public void end() {
      mAnimator.end();
    }

    @Override
    public long getDuration() {
      return mAnimator.getDuration();
    }

    @Override
    public TimeInterpolator getInterpolator() {
      return mAnimator.getInterpolator();
    }

    @Override
    public void setInterpolator(TimeInterpolator timeInterpolator) {
      mAnimator.setInterpolator(timeInterpolator);
    }

    @Override
    public ArrayList<AnimatorListener> getListeners() {
      return new ArrayList<>(mListeners.keySet());
    }

    @Override
    public long getStartDelay() {
      return mAnimator.getStartDelay();
    }

    @Override
    public void setStartDelay(long delayMS) {
      mAnimator.setStartDelay(delayMS);
    }

    @Override
    public boolean isPaused() {
      return mAnimator.isPaused();
    }

    @Override
    public boolean isRunning() {
      return mAnimator.isRunning();
    }

    @Override
    public boolean isStarted() {
      return mAnimator.isStarted();
    }

    /* We don't want to override pause or resume methods because we don't want them
     * to affect animator.
    public void pause();
    public void resume();
    public void addPauseListener(AnimatorPauseListener listener);
    public void removePauseListener(AnimatorPauseListener listener);
    */

    @Override
    public void removeAllListeners() {
      mListeners.clear();
      mAnimator.removeAllListeners();
    }

    @Override
    public void removeListener(AnimatorListener listener) {
      AnimatorListener wrapper = mListeners.get(listener);
      if (wrapper != null) {
        mListeners.remove(listener);
        mAnimator.removeListener(wrapper);
      }
    }

    @Override
    public Animator setDuration(long durationMS) {
      mAnimator.setDuration(durationMS);
      return this;
    }

    @Override
    public void setTarget(Object target) {
      mAnimator.setTarget(target);
    }

    @Override
    public void setupEndValues() {
      mAnimator.setupEndValues();
    }

    @Override
    public void setupStartValues() {
      mAnimator.setupStartValues();
    }

    @Override
    public void start() {
      mAnimator.start();
    }
  }

  private static class AnimatorListenerWrapper implements Animator.AnimatorListener {
    private final Animator animator;
    private final Animator.AnimatorListener listener;

    AnimatorListenerWrapper(Animator animator, Animator.AnimatorListener listener) {
      this.animator = animator;
      this.listener = listener;
    }

    @Override
    public void onAnimationStart(Animator animator) {
      listener.onAnimationStart(this.animator);
    }

    @Override
    public void onAnimationEnd(Animator animator) {
      listener.onAnimationEnd(this.animator);
    }

    @Override
    public void onAnimationCancel(Animator animator) {
      listener.onAnimationCancel(this.animator);
    }

    @Override
    public void onAnimationRepeat(Animator animator) {
      listener.onAnimationRepeat(this.animator);
    }
  }
}
