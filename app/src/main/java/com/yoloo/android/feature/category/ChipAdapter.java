package com.yoloo.android.feature.category;

import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.data.model.Chipable;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import java.util.List;

public class ChipAdapter<T extends Chipable> extends SelectableAdapter {

  private final OnItemSelectListener<T> onItemSelectListener;
  private int backgroundDrawableRes = -1;

  public ChipAdapter(OnItemSelectListener<T> onItemSelectListener) {
    this.onItemSelectListener = onItemSelectListener;
  }

  public void setBackgroundDrawable(@DrawableRes int backgroundDrawableRes) {
    this.backgroundDrawableRes = backgroundDrawableRes;
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
  public static abstract class ChipModel<T extends Chipable>
      extends EpoxyModelWithHolder<ChipModel.CategoryChipHolder> {

    @EpoxyAttribute T chipItem;
    @EpoxyAttribute(hash = false) OnItemSelectListener<T> onItemSelectListener;
    @EpoxyAttribute(hash = false) ChipAdapter adapter;
    @EpoxyAttribute(hash = false) @DrawableRes int backgroundDrawableRes;

    @Override public void bind(CategoryChipHolder holder) {
      holder.tvChip.setText(chipItem.getName());
      holder.tvChip.setSelected(adapter.isSelected(this));

      if (backgroundDrawableRes != -1) {
        holder.tvChip.setBackground(
            ContextCompat.getDrawable(holder.itemView.getContext(), backgroundDrawableRes));
      }

      holder.itemView.setOnClickListener(v -> {
        adapter.toggleSelection(this);
        onItemSelectListener.onItemSelect(v, this, chipItem, adapter.isSelected(this));
      });
    }

    public T getChipItem() {
      return chipItem;
    }

    static class CategoryChipHolder extends BaseEpoxyHolder {
      @BindView(R.id.tv_item_chip) TextView tvChip;
    }
  }
}
