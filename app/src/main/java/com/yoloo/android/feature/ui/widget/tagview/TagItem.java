package com.yoloo.android.feature.ui.widget.tagview;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import com.yoloo.android.R;
import java.util.Locale;

/**
 * Object representing HashtagView item model.
 *
 * @param <T> custom data model
 */
class TagItem<T> implements Comparable<TagItem> {

  protected T data;

  protected View view;
  protected float width;
  protected boolean isSelected;

  public static <T> TagItem<T> create(T data) {
    return new TagItem<>(data);
  }

  private TagItem(T data) {
    this.data = data;
  }

  void setText(CharSequence charSequence) {
    ((TextView) view.findViewById(R.id.text)).setText(charSequence);
  }

  void displaySelection(int left, int leftSelected, int right, int rightSelected) {
    ((TextView) view.findViewById(R.id.text)).setCompoundDrawablesWithIntrinsicBounds(
        isSelected ? leftSelected : left, 0, isSelected ? rightSelected : right, 0);
    view.setSelected(isSelected);
    view.invalidate();
  }

  void select(int left, int leftSelected, int right, int rightSelected) {
    isSelected = !isSelected;
    displaySelection(left, leftSelected, right, rightSelected);
  }

  void decorateText(TagView.DataTransform<T> transformer) {
    if (transformer instanceof TagView.DataStateTransform) {
      if (isSelected) {
        setText(((TagView.DataStateTransform<T>) transformer).prepareSelected(data));
      } else {
        setText(transformer.prepare(data));
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof TagItem && this.data.equals(((TagItem) o).data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  @Override
  public String toString() {
    return String.format(Locale.ENGLISH, "Item data: title - %s, width - %f", data.toString(),
        width);
  }

  @Override
  public int compareTo(@NonNull TagItem another) {
    if (width == another.width) return 0;
    return width < another.width ? 1 : -1;
  }
}