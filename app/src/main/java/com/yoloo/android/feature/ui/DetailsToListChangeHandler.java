package com.yoloo.android.feature.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.ControllerChangeHandler;

public class DetailsToListChangeHandler extends ControllerChangeHandler {

  private ControllerChangeCompletedListener changeListener;
  private View detailsView;
  private RecyclerView listView;
  private ViewGroup container;

  private int position = 0;

  private int viewHoldersLaidOut = 0;

  public DetailsToListChangeHandler() {
  }

  public DetailsToListChangeHandler(int position) {
    this.position = position;
  }

  @Override public void saveToBundle(@NonNull Bundle bundle) {
    super.saveToBundle(bundle);
    bundle.putInt("POSITION", position);
  }

  @Override public void restoreFromBundle(@NonNull Bundle bundle) {
    super.restoreFromBundle(bundle);
    position = bundle.getInt("POSITION");
  }

  @Override
  public void performChange(@NonNull ViewGroup container, @Nullable View from, @Nullable View to,
      boolean isPush,
      @NonNull ControllerChangeCompletedListener changeListener) {

    if (from != null && to != null) {
      this.changeListener = changeListener;
      this.detailsView = from;
      this.listView = (RecyclerView) to;
      this.container = container;

      final int size = container.getChildCount();
      // Find the index listView add the listView view
      for (int i = 0; i < size; i++) {
        if (container.getChildAt(i) == from) {
          // Add the listByCategory view in the hierarchy after the details view,
          // so that both are in the container, but the details view is in the front and therefore
          // the listByCategory view is not visible because hidden behind details view
          container.addView(listView, i);
          break;
        }
      }
    } else {
      // Not sure if this can ever be the case
      changeListener.onChangeCompleted();
    }
  }

  private void onViewHolderLaidOut(int position, RecyclerView.ViewHolder viewHolder) {
    viewHoldersLaidOut++;

    if (position != this.position) {
      if (viewHoldersLaidOut == listView.getChildCount()) {
        // If we do a screen orientation change, the target ViewHolder might not be visible anymore.
        container.removeView(detailsView);
        notifyTransitionEndAndCleanUp();
      }

      return;
    }

    AutoTransition transition = new AutoTransition();
    transition.addListener(new Transition.TransitionListener() {
      @Override public void onTransitionStart(@NonNull Transition transition) {

      }

      @Override public void onTransitionEnd(@NonNull Transition transition) {
        notifyTransitionEndAndCleanUp();
      }

      @Override public void onTransitionCancel(@NonNull Transition transition) {
        notifyTransitionEndAndCleanUp();
      }

      @Override public void onTransitionPause(@NonNull Transition transition) {

      }

      @Override public void onTransitionResume(@NonNull Transition transition) {

      }
    });

    listView.removeView(viewHolder.itemView);
    TransitionManager.beginDelayedTransition(container, transition);
    container.removeView(detailsView);
    listView.addView(viewHolder.itemView);
  }

  private void notifyTransitionEndAndCleanUp() {
    // To avoid memory leaks
    detailsView = null;
    listView = null;
    container = null;
    changeListener.onChangeCompleted();
    changeListener = null;
    viewHoldersLaidOut = 0;
  }
}
