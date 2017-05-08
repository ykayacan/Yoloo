package com.yoloo.android.ui.recyclerview;

import android.view.View;

public interface OnItemClickListener<M> {

  void onItemClick(View v, M item);
}
