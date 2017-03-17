package com.yoloo.android.ui.widget.linkabletextview;

public interface LinkableCallback {
  void onMatch(@LinkType int type, String value);
}
