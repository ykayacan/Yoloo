package com.yoloo.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import io.reactivex.Observable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class KeyboardUtil implements ViewTreeObserver.OnGlobalLayoutListener {

  private static Map<SoftKeyboardToggleListener, KeyboardUtil> sListenerMap = new HashMap<>();
  private SoftKeyboardToggleListener mCallback;
  private View mRootView;
  private float mScreenDensity = 1;
  private Rect r = new Rect();

  private KeyboardUtil(Activity act, SoftKeyboardToggleListener listener) {
    mCallback = listener;

    mRootView = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);
    if (mRootView != null) {
      mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    mScreenDensity = Resources.getSystem().getDisplayMetrics().density;
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
    Observable.just(imm)
        .delay(delay, TimeUnit.MILLISECONDS)
        .subscribe(inputMethodManager -> inputMethodManager.showSoftInput(view,
            InputMethodManager.SHOW_IMPLICIT));
  }

  public static void addKeyboardToggleListener(Activity act, SoftKeyboardToggleListener listener) {
    removeKeyboardToggleListener(listener);

    sListenerMap.put(listener, new KeyboardUtil(act, listener));
  }

  public static void removeKeyboardToggleListener(SoftKeyboardToggleListener listener) {
    if (sListenerMap.containsKey(listener)) {
      KeyboardUtil k = sListenerMap.get(listener);
      k.removeListener();

      sListenerMap.remove(listener);
    }
  }

  public static void removeAllKeyboardToggleListeners() {
    for (SoftKeyboardToggleListener l : sListenerMap.keySet()) {
      sListenerMap.get(l).removeListener();
    }

    sListenerMap.clear();
  }

  @Override
  public void onGlobalLayout() {
    //r will be populated with the coordinates get your view that area still visible.
    mRootView.getWindowVisibleDisplayFrame(r);

    int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);
    float dp = heightDiff / mScreenDensity;

    if (mCallback != null) {
      mCallback.onToggleSoftKeyboard(dp > 200);
    }
  }

  private void removeListener() {
    mCallback = null;

    mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
  }

  public interface SoftKeyboardToggleListener {
    void onToggleSoftKeyboard(boolean isVisible);
  }
}
