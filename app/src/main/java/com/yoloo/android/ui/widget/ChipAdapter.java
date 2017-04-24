package com.yoloo.android.ui.widget;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.data.model.Chipable;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import java.util.List;

public class ChipAdapter<T extends Chipable> extends SelectableAdapter {

  private final OnItemSelectListener<T> onItemSelectListener;
  private int backgroundDrawableRes;
  private int textColor;

  public ChipAdapter(OnItemSelectListener<T> onItemSelectListener) {
    this.onItemSelectListener = onItemSelectListener;
  }

  public void setBackgroundDrawable(@DrawableRes int backgroundDrawableRes) {
    this.backgroundDrawableRes = backgroundDrawableRes;
  }

  public void setTextColor(@ColorRes int textColor) {
    this.textColor = textColor;
  }

  public void addChipItems(List<T> items) {
    List<ChipAdapter$ChipModel_<T>> chipModels = Stream.of(items)
        .map(item -> new ChipAdapter$ChipModel_<T>().chipItem(item)
            .adapter(this)
            .onItemSelectListener(onItemSelectListener)
            .backgroundDrawableRes(backgroundDrawableRes))
        .toList();

    addModels(chipModels);
  }

  public void clear() {
    removeAllModels();
  }

  public interface OnItemSelectListener<T> {
    void onItemSelect(View v, EpoxyModel<?> model, T item, boolean selected);
  }

  @EpoxyModelClass(layout = R.layout.item_chip)
  public static abstract class ChipModel<T extends Chipable> extends EpoxyModel<TextView> {

    @EpoxyAttribute T chipItem;
    @EpoxyAttribute(hash = false) OnItemSelectListener<T> onItemSelectListener;
    @EpoxyAttribute(hash = false) ChipAdapter adapter;
    @EpoxyAttribute(hash = false) @DrawableRes int backgroundDrawableRes;
    @EpoxyAttribute(hash = false) @ColorRes int textColorRes;

    @Override public void bind(TextView view) {
      view.setText(chipItem.getName());
      view.setSelected(adapter.isSelected(this));
      view.setTextSize(14F);
      view.setGravity(Gravity.CENTER);
      view.setBackground(ContextCompat.getDrawable(view.getContext(), backgroundDrawableRes));

      view.setOnClickListener(v -> {
        adapter.toggleSelection(this);
        onItemSelectListener.onItemSelect(v, this, chipItem, adapter.isSelected(this));
      });
    }

    public T getChipItem() {
      return chipItem;
    }
  }
}
