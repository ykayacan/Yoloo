package com.yoloo.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import butterknife.ButterKnife;

public final class KeyboardUtil implements ViewTreeObserver.OnGlobalLayoutListener {

  private static ArrayMap<SoftKeyboardToggleListener, KeyboardUtil> listeners = new ArrayMap<>();
  private SoftKeyboardToggleListener listener;
  private View rootView;
  private float screenDensity;
  private Rect rect = new Rect();

  private KeyboardUtil(Activity activity, SoftKeyboardToggleListener listener) {
    this.listener = listener;
    rootView = ((ViewGroup) ButterKnife.findById(activity, android.R.id.content)).getChildAt(0);
    if (rootView != null) {
      rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    screenDensity = Resources.getSystem().getDisplayMetrics().density;
  }

  /**
   * Hide keyboard.
   *
   * <pre>
   * <code>KeyboardUtil.hideKeyboard(getActivity(), searchField);</code>
   * </pre>
   */
  public static void hideKeyboard(@NonNull View view) {
    InputMethodManager imm = (InputMethodManager) view.getContext().getApplicationContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  /**
   * Show keyboard with a 100ms delay.
   *
   * <pre>
   * <code>KeyboardUtil.showDelayedKeyboard(getActivity(), searchField);</code>
   * </pre>
   */
  public static void showDelayedKeyboard(@NonNull View view) {
    showDelayedKeyboard(view, 100);
  }

  /**
   * Show keyboard with a custom delay.
   *
   * <pre>
   * <code>KeyboardUtil.showDelayedKeyboard(getActivity(), searchField, 500);</code>
   * </pre>
   */
  public static void showDelayedKeyboard(@NonNull View view, int delay) {
    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    new WeakHandler().postDelayed(() ->
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT), delay);
  }

  public static void addKeyboardToggleListener(Activity activity,
      SoftKeyboardToggleListener listener) {
    removeKeyboardToggleListener(listener);

    listeners.put(listener, new KeyboardUtil(activity, listener));
  }

  public static void removeKeyboardToggleListener(SoftKeyboardToggleListener listener) {
    if (listeners.containsKey(listener)) {
      KeyboardUtil util = listeners.get(listener);
      util.removeListener();

      listeners.remove(listener);
    }
  }

  public static void removeAllKeyboardToggleListeners() {
    for (SoftKeyboardToggleListener l : listeners.keySet()) {
      listeners.get(l).removeListener();
    }

    listeners.clear();
  }

  @Override
  public void onGlobalLayout() {
    //rect will be populated with the coordinates getPost your view that area still visible.
    rootView.getWindowVisibleDisplayFrame(rect);

    final int heightDiff = rootView.getRootView().getHeight() - (rect.bottom - rect.top);
    final float dp = heightDiff / screenDensity;

    if (listener != null) {
      listener.onToggleSoftKeyboard(dp > 200);
    }
  }

  private void removeListener() {
    listener = null;

    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
  }

  public interface SoftKeyboardToggleListener {
    void onToggleSoftKeyboard(boolean isVisible);
  }
}
