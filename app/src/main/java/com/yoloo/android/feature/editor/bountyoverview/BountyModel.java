package com.yoloo.android.feature.editor.bountyoverview;

import android.graphics.drawable.Drawable;
import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;

@EpoxyModelClass(layout = R.layout.item_bounty)
abstract class BountyModel extends EpoxyModel<TextView> {

  @EpoxyAttribute(hash = false) BountyAdapter adapter;
  @EpoxyAttribute String value;
  @EpoxyAttribute Drawable drawable;
  @EpoxyAttribute OnBountyClickListener onBountyClickListener;

  @Override public void bind(TextView view) {
    view.setText(value);
    view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

    final boolean isSelected = adapter.isSelected(this);
    view.setSelected(isSelected);

    view.setOnClickListener(v -> {
      if (adapter.canSelect(this)) {
        adapter.toggleSelection(this);
        onBountyClickListener.onBountyClick(Integer.parseInt(value));
      }
    });
  }
}
