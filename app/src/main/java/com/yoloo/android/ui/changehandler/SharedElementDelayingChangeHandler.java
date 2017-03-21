package com.yoloo.android.ui.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * A TransitionChangeHandler that will wait for views with the passed transition names to be fully
 * laid out before executing. An OnPreDrawListener will be added to the "to" view, then to all from
 * its subviews that match the transaction names we're interested in. Once all from the views are
 * fully ready, the "to" view is set to invisible so that it'll fade in nicely, and the views that
 * we want to use as shared elements are removed from their containers, then immediately re-added
 * within the beginDelayedTransition call so the system picks them up as shared elements.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SharedElementDelayingChangeHandler extends ArcFadeMoveChangeHandler {

  private static final String KEY_WAIT_FOR_TRANSITION_NAMES =
      "SharedElementDelayingChangeHandler.waitForTransitionNames";

  private final ArrayList<String> waitForTransitionNames;
  private final ArrayList<ViewParentPair> removedViews = new ArrayList<>();
  private ViewTreeObserver.OnPreDrawListener onPreDrawListener;

  public SharedElementDelayingChangeHandler() {
    waitForTransitionNames = new ArrayList<>();
  }

  public SharedElementDelayingChangeHandler(@NonNull List<String> waitForTransitionNames) {
    this.waitForTransitionNames = new ArrayList<>(waitForTransitionNames);
  }

  @Override
  public void prepareForTransition(@NonNull final ViewGroup container, @Nullable View from,
      @Nullable final View to, @NonNull
      Transition transition, boolean isPush,
      @NonNull final OnTransitionPreparedListener onTransitionPreparedListener) {
    if (to != null && to.getParent() == null && waitForTransitionNames.size() > 0) {
      onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        boolean addedSubviewListeners;

        @Override
        public boolean onPreDraw() {
          List<View> foundViews = Stream.of(waitForTransitionNames)
              .map(transitionName -> getViewWithTransitionName(to, transitionName))
              .toList();

          if (!foundViews.contains(null) && !addedSubviewListeners) {
            addedSubviewListeners = true;

            for (final View view : foundViews) {
              view.getViewTreeObserver()
                  .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                      view.getViewTreeObserver().removeOnPreDrawListener(this);
                      waitForTransitionNames.remove(view.getTransitionName());

                      ViewGroup parent = (ViewGroup) view.getParent();
                      removedViews.add(new ViewParentPair(view, parent));
                      parent.removeView(view);

                      if (waitForTransitionNames.size() == 0) {
                        to.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);

                        to.setVisibility(View.INVISIBLE);

                        onTransitionPreparedListener.onPrepared();
                      }
                      return true;
                    }
                  });
            }
          }

          return false;
        }
      };

      to.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);

      container.addView(to);
    } else {
      onTransitionPreparedListener.onPrepared();
    }
  }

  @Override public void executePropertyChanges(@NonNull ViewGroup container, @Nullable View from,
      @Nullable View to, @NonNull Transition transition, boolean isPush) {
    if (to != null) {
      to.setVisibility(View.VISIBLE);

      for (ViewParentPair removedView : removedViews) {
        removedView.parent.addView(removedView.view);
      }

      removedViews.clear();
    }

    super.executePropertyChanges(container, from, to, transition, isPush);
  }

  @Override public void saveToBundle(@NonNull Bundle bundle) {
    bundle.putStringArrayList(KEY_WAIT_FOR_TRANSITION_NAMES, waitForTransitionNames);
  }

  @Override public void restoreFromBundle(@NonNull Bundle bundle) {
    List<String> savedNames = bundle.getStringArrayList(KEY_WAIT_FOR_TRANSITION_NAMES);
    if (savedNames != null) {
      waitForTransitionNames.addAll(savedNames);
    }
  }

  @Override public void onAbortPush(@NonNull ControllerChangeHandler newHandler,
      @Nullable Controller newTop) {
    super.onAbortPush(newHandler, newTop);

    removedViews.clear();
  }

  @Nullable
  private View getViewWithTransitionName(@NonNull View view, @NonNull String transitionName) {
    if (transitionName.equals(view.getTransitionName())) {
      return view;
    }

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      int childCount = viewGroup.getChildCount();

      for (int i = 0; i < childCount; i++) {
        View viewWithTransitionName =
            getViewWithTransitionName(viewGroup.getChildAt(i), transitionName);
        if (viewWithTransitionName != null) {
          return viewWithTransitionName;
        }
      }
    }

    return null;
  }

  private static class ViewParentPair {
    View view;
    ViewGroup parent;

    ViewParentPair(View view, ViewGroup parent) {
      this.view = view;
      this.parent = parent;
    }
  }
}
