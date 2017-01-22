package com.yoloo.android.feature.search;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;

public class TagModel extends EpoxyModelWithHolder<TagModel.TagViewHolder> {

  @EpoxyAttribute TagRealm tag;

  @EpoxyAttribute(hash = false) OnTagClickListener onTagClickListener;

  @Override protected TagViewHolder createNewHolder() {
    return new TagViewHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_search_tag;
  }

  @Override public void bind(TagViewHolder holder) {
    final Resources res = holder.tvTagCount.getResources();

    holder.tvTag.setText(tag.getName());
    holder.tvTagCount.setText(res.getString(R.string.label_search_post_count, tag.getPosts()));

    holder.viewGroup.setOnClickListener(v -> onTagClickListener.onTagClick(tag.getName()));
  }

  @Override public void unbind(TagViewHolder holder) {
    holder.tvTag.setOnClickListener(null);
  }

  static class TagViewHolder extends BaseEpoxyHolder {
    @BindView(R.id.layout_item_search) ViewGroup viewGroup;

    @BindView(R.id.tv_item_search_tag) TextView tvTag;

    @BindView(R.id.tv_item_search_post_count) TextView tvTagCount;
  }
}
