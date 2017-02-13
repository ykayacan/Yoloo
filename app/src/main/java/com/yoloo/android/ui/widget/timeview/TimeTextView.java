package com.yoloo.android.ui.widget.timeview;

import android.content.Context;
import android.util.AttributeSet;
import com.yoloo.android.ui.widget.BaselineGridTextView;

public class TimeTextView extends BaselineGridTextView {

  private long timestamp;

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
    setText(TimeManager.getInstance().calculateTime(this.timestamp));
    TimeWatcher.getInstance().attach(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    TimeWatcher.getInstance().detach(this);
  }

  void update() {
    setText(TimeManager.getInstance().calculateTime(timestamp));
  }

  public long getTimestamp() {
    return timestamp;
  }
}
