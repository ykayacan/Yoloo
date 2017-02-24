package com.yoloo.android.feature.category;

import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_category_chip)
public abstract class CategoryChipModel extends
    EpoxyModelWithHolder<CategoryChipModel.CategoryChipHolder> {

  @EpoxyAttribute CategoryRealm category;
  @EpoxyAttribute(hash = false) OnItemClickListener<CategoryRealm> onItemClickListener;
  @EpoxyAttribute(hash = false) CategoryChipAdapter adapter;

  @Override public void bind(CategoryChipHolder holder) {
    holder.tvChip.setText(category.getName());
    holder.tvChip.setSelected(adapter.isSelected(this));

    holder.itemView.setOnClickListener(v -> {
      adapter.toggleSelection(this);
      onItemClickListener.onItemClick(v, this, category);
    });
  }

  public CategoryRealm getCategory() {
    return category;
  }

  static class CategoryChipHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_item_category_chip) TextView tvChip;
  }
}
