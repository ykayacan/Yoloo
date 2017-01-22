package com.yoloo.android.feature.ui.widget.zamanview;

import android.content.Context;
import android.util.AttributeSet;
import com.yoloo.android.feature.ui.widget.CompatTextView;

public class ZamanTextView extends CompatTextView {

  private long timestamp;
  private TimeWatcher timeWatcher = TimeWatcher.getInstance();
  private ZamanManager zamanManager;

  public ZamanTextView(Context context) {
    super(context);
  }

  public ZamanTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ZamanTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setTimeStamp(long timestamp) {
    this.timestamp = timestamp;
    zamanManager = ZamanManager.getInstance(timestamp);
    setText(zamanManager.getTime());
    timeWatcher.attach(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    timeWatcher.detached(this);
  }

  public void update() {
    zamanManager.calculateTime(timestamp);
    setText(zamanManager.getTime());
  }

  public long getTimestamp() {
    return timestamp;
  }
}
