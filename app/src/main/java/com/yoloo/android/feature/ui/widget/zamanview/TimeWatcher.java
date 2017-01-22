package com.yoloo.android.feature.ui.widget.zamanview;

import com.yoloo.android.util.WeakHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimeWatcher {

  private static final WeakHandler HANDLER = new WeakHandler();
  private static TimeWatcher timeWatcher;
  private static List<ZamanTextView> textViews = new ArrayList<>();
  private static TimerTask timerTask;

  private TimeWatcher() {
  }

  public static TimeWatcher getInstance() {
    if (timeWatcher == null) {
      timeWatcher = new TimeWatcher();
      final Timer timer = new Timer();
      initializeTimerTask();
      timer.schedule(timerTask, 1000, 1000);
    }
    return timeWatcher;
  }

  public static void updateTextViews() {
    for (ZamanTextView textView : textViews) {
      textView.update();
    }
  }

  public static void initializeTimerTask() {
    timerTask = new TimerTask() {
      public void run() {
        HANDLER.post(TimeWatcher::updateTextViews);
      }
    };
  }

  public void attach(ZamanTextView textView) {
    if (!textViews.contains(textView)) {
      textViews.add(textView);
    }
  }

  public void detached(ZamanTextView textView) {
    if (textViews.contains(textView)) {
      textViews.remove(textView);
    }
  }
}