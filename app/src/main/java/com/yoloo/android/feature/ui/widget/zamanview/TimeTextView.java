package com.yoloo.android.feature.ui.widget.zamanview;

import android.content.Context;
import android.util.AttributeSet;
import com.yoloo.android.feature.ui.widget.CompatTextView;

public class TimeTextView extends CompatTextView {

  private static final TimeWatcher timeWatcher = TimeWatcher.getInstance();

  private long timestamp;

  private TimeManager timeManager;

  public TimeTextView(Context context) {
    super(context);
  }

  public TimeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TimeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setTimeStamp(long timestamp) {
    this.timestamp = timestamp;
    timeManager = TimeManager.getInstance(timestamp);
    setText(timeManager.getTime());
    timeWatcher.attach(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    timeWatcher.detach(this);
  }

  public void update() {
    timeManager.calculateTime(timestamp);
    setText(timeManager.getTime());
  }

  public long getTimestamp() {
    return timestamp;
  }
}
