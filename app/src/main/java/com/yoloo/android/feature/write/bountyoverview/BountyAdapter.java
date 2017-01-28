package com.yoloo.android.feature.write.bountyoverview;

import android.graphics.drawable.Drawable;
import com.yoloo.android.feature.ui.recyclerview.SelectableEpoxyAdapter;
import java.util.List;

public class BountyAdapter extends SelectableEpoxyAdapter {

  private final OnBountyClickListener onBountyClickListener;

  public BountyAdapter(OnBountyClickListener onBountyClickListener) {
    this.onBountyClickListener = onBountyClickListener;

    enableDiffing();
  }

  public void addAll(String[] values, List<Drawable> drawables) {
    final int size = values.length;
    for (int i = 0; i < size; i++) {
      models.add(new BountyModel_().value(values[i])
          .drawable(drawables.get(i))
          .adapter(this)
          .onBountyClickListener(onBountyClickListener));
    }

    notifyModelsChanged();
  }

  @Override protected int getMaxSelectedItems() {
    return 1;
  }

  public void selectBountyItem(int selectedBounty) {
    switch (selectedBounty) {
      case 10:
        select(models.get(0));
        break;
      case 20:
        select(models.get(1));
        break;
      case 30:
        select(models.get(2));
        break;
      case 40:
        select(models.get(3));
        break;
      case 50:
        select(models.get(4));
        break;
    }
  }
}
