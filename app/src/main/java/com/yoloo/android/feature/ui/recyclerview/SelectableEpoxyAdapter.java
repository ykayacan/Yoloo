package com.yoloo.android.feature.ui.recyclerview;

import android.support.v4.util.SparseArrayCompat;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectableEpoxyAdapter extends EpoxyAdapter {

  private SparseArrayCompat<EpoxyModel<?>> selectedItems;

  public SelectableEpoxyAdapter() {
    this.selectedItems = new SparseArrayCompat<>(5);
  }

  protected abstract int getMaxSelectedItems();

  /**
   * Indicates if the item at position position is selected
   *
   * @param model Position of the item to check
   * @return true if the item is selected, false otherwise
   */
  public boolean isSelected(EpoxyModel<?> model) {
    return selectedItems.indexOfValue(model) != -1;
  }

  /**
   * Toggle the selection status of the item at a given position
   *
   * @param model Position of the item to toggle the selection status for
   */
  public void toggleSelection(EpoxyModel<?> model) {
    final int id = (int) model.id();
    if (selectedItems.get(id, null) != null) {
      unSelect(model);
    } else {
      select(model);
    }
  }

  /**
   * Toggle the selection status of the items position contained in ArrayList<Integer>
   *
   * @param selectedModels Positions of the items to toggle the selection status for
   */
  public void setSelectedPositions(List<EpoxyModel<?>> selectedModels) {
    for (EpoxyModel<?> model : selectedModels) {
      toggleSelection(model);
    }
  }

  /**
   * Clear the selection status for all items
   */
  public void clearSelection() {
    List<EpoxyModel<?>> selection = getSelectedItems();
    selectedItems.clear();
    for (EpoxyModel<?> model : selection) {
      notifyModelChanged(model);
    }
  }

  public void select(EpoxyModel<?> model) {
    selectedItems.put((int) model.id(), model);
    notifyModelChanged(model);
  }

  public void unSelect(EpoxyModel<?> model) {
    selectedItems.delete((int) model.id());
    notifyModelChanged(model);
  }

  public void clearSelected() {
    selectedItems.clear();
  }

  /**
   * Count the selected items
   *
   * @return Selected items count
   */
  public int getSelectedItemCount() {
    return selectedItems.size();
  }

  public boolean canSelectItem(EpoxyModel<?> model) {
    return isSelected(model) || getSelectedItemCount() < getMaxSelectedItems();
  }

  /**
   * Indicates the search of selected items
   *
   * @return List of selected items ids
   */
  public List<EpoxyModel<?>> getSelectedItems() {
    final int size = selectedItems.size();
    List<EpoxyModel<?>> items = new ArrayList<>(size);
    for (int i = 0; i < size; ++i) {
      items.add(selectedItems.valueAt(i));
    }
    return items;
  }
}
