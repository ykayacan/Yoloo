package com.yoloo.android.feature.write.bountyoverview;

import android.graphics.drawable.Drawable;
import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;

public class BountyModel extends EpoxyModel<TextView> {

  @EpoxyAttribute(hash = false) BountyAdapter adapter;

  @EpoxyAttribute String value;

  @EpoxyAttribute Drawable drawable;

  @EpoxyAttribute OnBountyClickListener onBountyClickListener;

  @Override protected int getDefaultLayout() {
    return R.layout.item_bounty;
  }

  @Override public void bind(TextView view) {
    view.setText(value);
    view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

    final boolean isSelected = adapter.isSelected(this);
    view.setSelected(isSelected);

    view.setOnClickListener(v -> {
      if (adapter.canSelectItem(this)) {
        adapter.toggleSelection(this);
        onBountyClickListener.onBountyClick(Integer.parseInt(value));
      }
    });
  }

  @Override public void unbind(TextView view) {
    view.setOnClickListener(null);
  }
}
