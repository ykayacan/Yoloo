package com.yoloo.android.feature.editor.bountyoverview;

import android.graphics.drawable.Drawable;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import java.util.List;

class BountyAdapter extends SelectableAdapter {

  private final OnBountyClickListener onBountyClickListener;

  BountyAdapter(OnBountyClickListener onBountyClickListener) {
    this.onBountyClickListener = onBountyClickListener;
    setMaxSelection(1);
  }

  void addAll(String[] values, List<Drawable> drawables) {
    final int size = values.length;
    for (int i = 0; i < size; i++) {
      addModel(new BountyModel_()
          .value(values[i])
          .drawable(drawables.get(i))
          .adapter(this)
          .onBountyClickListener(onBountyClickListener));
    }
  }

  void selectBountyItem(int selectedBounty) {
    switch (selectedBounty) {
      case 10:
        toggleSelection(models.get(0));
        break;
      case 20:
        toggleSelection(models.get(1));
        break;
      case 30:
        toggleSelection(models.get(2));
        break;
      case 40:
        toggleSelection(models.get(3));
        break;
      case 50:
        toggleSelection(models.get(4));
        break;
    }
  }
}
