package com.yoloo.android.ui.recyclerview;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;

public interface OnItemLongClickListener<M> {

  void onItemLongClick(View v, EpoxyModel<?> model, M item);
}
