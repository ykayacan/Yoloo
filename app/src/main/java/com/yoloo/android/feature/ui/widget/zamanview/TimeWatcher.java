package com.yoloo.android.feature.ui.widget.zamanview;

import com.yoloo.android.util.WeakHandler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class TimeWatcher {

  private static WeakHandler handler;
  private static List<WeakReference<TimeTextView>> views;
  private static TimeWatcher timeWatcher;
  private static TimerTask timerTask;

  private TimeWatcher() {
  }

  public static TimeWatcher getInstance() {
    if (timeWatcher == null) {
      timeWatcher = new TimeWatcher();
      handler= new WeakHandler();
      views = new ArrayList<>();
      final Timer timer = new Timer();
      initializeTimerTask();
      timer.schedule(timerTask, 1000, 1000);
    }
    return timeWatcher;
  }

  public static void updateTime() {
    for (WeakReference<TimeTextView> ref : views) {
      if (ref != null) {
        final TimeTextView view = ref.get();
        if (view != null) {
          view.update();
        }
      }
    }
  }

  public static void initializeTimerTask() {
    timerTask = new TimerTask() {
      @Override public void run() {
        handler.post(TimeWatcher::updateTime);
      }
    };
  }

  public void attach(TimeTextView view) {
    views.add(new WeakReference<>(view));
  }

  public void detach(TimeTextView view) {
    views.remove(new WeakReference<>(view));
  }
}
