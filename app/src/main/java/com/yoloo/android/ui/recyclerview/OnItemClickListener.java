package com.yoloo.android.ui.recyclerview;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;

public interface OnItemClickListener<M> {

  void onItemClick(View v, EpoxyModel<?> model, M item);
}
