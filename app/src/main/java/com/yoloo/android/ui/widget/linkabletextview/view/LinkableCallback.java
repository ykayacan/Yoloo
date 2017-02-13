package com.yoloo.android.ui.widget.linkabletextview.view;

import com.yoloo.android.ui.widget.linkabletextview.annotation.LinkType;

public interface LinkableCallback {
  void onMatch(@LinkType int type, String value);
}
