package com.yoloo.android.feature.search;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_search_tag)
public abstract class TagModel extends EpoxyModelWithHolder<TagModel.TagViewHolder> {

  @EpoxyAttribute TagRealm tag;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;

  @Override public void bind(TagViewHolder holder) {
    final Resources res = holder.itemView.getResources();

    holder.tvTag.setText(tag.getName());
    holder.tvTagCount.setText(res.getString(R.string.label_search_post_count, tag.getPostCount()));

    holder.itemView.setOnClickListener(onClickListener);
  }

  @Override public void unbind(TagViewHolder holder) {
    holder.tvTag.setOnClickListener(null);
  }

  static class TagViewHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_item_search_tag) TextView tvTag;
    @BindView(R.id.tv_item_search_post_count) TextView tvTagCount;
  }
}
