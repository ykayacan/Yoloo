package com.yoloo.android.feature.ui.widget.timeview;

import android.content.Context;
import android.util.AttributeSet;
import com.yoloo.android.feature.ui.widget.CompatTextView;

public class ZamanTextView extends CompatTextView {

  private long timestamp;
  private TimeWatcher timeWatcher = TimeWatcher.getInstance();
  private ZamanUtil zamanUtil;

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
    zamanUtil = new ZamanUtil(timestamp);
    setText(zamanUtil.getTime());
    timeWatcher.attach(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    timeWatcher.detached(this);
  }

  public void update() {
    zamanUtil.calculateTime(timestamp);
    setText(zamanUtil.getTime());
  }

  public long getTimestamp() {
    return timestamp;
  }
}
