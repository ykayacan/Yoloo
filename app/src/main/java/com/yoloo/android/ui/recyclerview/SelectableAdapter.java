package com.yoloo.android.ui.recyclerview;

import android.support.v4.util.SparseArrayCompat;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter extends EpoxyAdapter implements Selectable {

  private final SparseArrayCompat<EpoxyModel<?>> selectedItems;
  private final List<EpoxyModel<?>> selectedItemsList = new ArrayList<>();

  private OnMaxSelectionReachedListener onMaxSelectionReachedListener;
  private OnSelectionListener onSelectionListener;

  private int maxSelection = Integer.MAX_VALUE;

  public SelectableAdapter() {
    this.selectedItems = new SparseArrayCompat<>();
  }

  @Override
  public void clearSelection() {
    List<EpoxyModel<?>> items = getSelectedItems();
    selectedItems.clear();
    for (EpoxyModel<?> model : items) {
      notifyModelChanged(model);
    }
  }

  @Override
  public int getSelectedItemCount() {
    return selectedItems.size();
  }

  @Override
  public List<EpoxyModel<?>> getSelectedItems() {
    final int size = getSelectedItemCount();
    selectedItemsList.clear();
    for (int i = 0; i < size; i++) {
      selectedItemsList.add(selectedItems.valueAt(i));
    }
    return selectedItemsList;
  }

  @Override
  public boolean isMaxSelectionReached() {
    return getSelectedItemCount() == maxSelection;
  }

  @Override
  public boolean isSelected(EpoxyModel<?> model) {
    return selectedItems.indexOfValue(model) != -1;
  }

  @Override
  public boolean canSelect(EpoxyModel<?> model) {
    return isSelected(model) || getSelectedItemCount() < maxSelection;
  }

  @Override
  public boolean toggleSelection(EpoxyModel<?> model) {
    final int id = (int) model.id();
    if (selectedItems.get(id) != null) {
      selectedItems.delete(id);
      notifyModelChanged(model);
      if (onSelectionListener != null) {
        onSelectionListener.onSelect(model, false);
      }
      return true;
    } else {
      if (getSelectedItemCount() == maxSelection) {
        if (onMaxSelectionReachedListener != null) {
          onMaxSelectionReachedListener.onMaxSelectionReached();
        }
        return false;
      } else {
        selectedItems.put(id, model);
        notifyModelChanged(model);
        if (onSelectionListener != null) {
          onSelectionListener.onSelect(model, true);
        }
        return true;
      }
    }
  }

  @Override
  public void setMaxSelection(int maxSelection) {
    this.maxSelection = maxSelection;
  }

  public void setOnMaxSelectionReachedListener(
      OnMaxSelectionReachedListener onMaxSelectionReachedListener) {
    this.onMaxSelectionReachedListener = onMaxSelectionReachedListener;
  }

  public void setOnSelectionListener(OnSelectionListener onSelectionListener) {
    this.onSelectionListener = onSelectionListener;
  }

  public interface OnSelectionListener {
    void onSelect(EpoxyModel<?> model, boolean selected);
  }
}
