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
import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Property;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
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

  private static ArgbEvaluator sArgbEvaluator;

  private AnimUtils() {
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

  public static Interpolator getOvershoot() {
    if (overshoot == null) {
      overshoot = new OvershootInterpolator();
    }
    return overshoot;
  }

  public static Interpolator getAnticipate() {
    if (anticipate == null) {
      anticipate = new AnticipateInterpolator();
    }
    return anticipate;
  }

  public static ArgbEvaluator getArgbEvaluator() {
    if (sArgbEvaluator == null) {
      sArgbEvaluator = new ArgbEvaluator();
    }
    return sArgbEvaluator;
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
   * An implementation of {@link Property} to be used specifically with fields of type
   * <code>float</code>. This type-specific subclass enables performance benefit by allowing calls
   * to a {@link #set(Object, Float) set()} function that takes the primitive <code>float</code>
   * type and avoids autoboxing and other overhead associated with the <code>Float</code> class.
   *
   * @param <T> The class on which the Property is declared.
   **/
  public abstract static class FloatProperty<T> extends Property<T, Float> {
    public FloatProperty(String name) {
      super(Float.class, name);
    }

    /**
     * A type-specific override of the {@link #set(Object, Float)} that is faster when dealing with
     * fields of type <code>float</code>.
     */
    public abstract void setValue(T object, float value);

    @Override
    public final void set(T object, Float value) {
      setValue(object, value);
    }
  }

  /**
   * An implementation of {@link Property} to be used specifically with fields of type
   * <code>int</code>. This type-specific subclass enables performance benefit by allowing calls to
   * a {@link #set(Object, Integer) set()} function that takes the primitive <code>int</code> type
   * and avoids autoboxing and other overhead associated with the <code>Integer</code> class.
   *
   * @param <T> The class on which the Property is declared.
   */
  public abstract static class IntProperty<T> extends Property<T, Integer> {

    public IntProperty(String name) {
      super(Integer.class, name);
    }

    /**
     * A type-specific override of the {@link #set(Object, Integer)} that is faster when dealing
     * with fields of type <code>int</code>.
     */
    public abstract void setValue(T object, int value);

    @Override
    public final void set(T object, Integer value) {
      setValue(object, value);
    }
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
      return new ArrayList<AnimatorListener>(mListeners.keySet());
    }

    @Override
    public long getStartDelay() {
      return mAnimator.getStartDelay();
    }

    @Override
    public void setStartDelay(long delayMS) {
      mAnimator.setStartDelay(delayMS);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
         * to affect mAnimator.
        public void pause();

        public void resume();

        public void addPauseListener(AnimatorPauseListener mListener);

        public void removePauseListener(AnimatorPauseListener mListener);
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
    private final Animator mAnimator;
    private final Animator.AnimatorListener mListener;

    public AnimatorListenerWrapper(Animator animator, Animator.AnimatorListener listener) {
      mAnimator = animator;
      mListener = listener;
    }

    @Override
    public void onAnimationStart(Animator animator) {
      mListener.onAnimationStart(mAnimator);
    }

    @Override
    public void onAnimationEnd(Animator animator) {
      mListener.onAnimationEnd(mAnimator);
    }

    @Override
    public void onAnimationCancel(Animator animator) {
      mListener.onAnimationCancel(mAnimator);
    }

    @Override
    public void onAnimationRepeat(Animator animator) {
      mListener.onAnimationRepeat(mAnimator);
    }
  }

  public static class AnimationListenerAdapter implements Animation.AnimationListener {
    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
  }
}
