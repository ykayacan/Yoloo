package com.yoloo.android.ui.recyclerview;

import com.airbnb.epoxy.EpoxyModel;
import java.util.List;

public interface Selectable {

  void clearSelection();

  int getSelectedItemCount();

  List<EpoxyModel<?>> getSelectedItems();

  boolean isMaxSelectionReached();

  boolean isSelected(EpoxyModel<?> model);

  boolean canSelect(EpoxyModel<?> model);

  boolean toggleSelection(EpoxyModel<?> model);

  void setMaxSelection(int maxSelection);
}
