package com.yoloo.android.feature.feed.common.model;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseEpoxyHolder;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;

public class CategoryModel extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

  @EpoxyAttribute(hash = false)
  FeedAdapter.OnCategoryClickListener onCategoryClickListener;

  @EpoxyAttribute
  CategoryRealm realm;

  @Override
  protected CategoryHolder createNewHolder() {
    return new CategoryHolder();
  }

  @Override
  protected int getDefaultLayout() {
    return R.layout.item_feed_category;
  }

  @Override
  public void bind(CategoryHolder holder) {
    Glide.with(holder.ivTopicBackground.getContext().getApplicationContext())
        .load(realm.getBackgroundUrl())
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(holder.ivTopicBackground);

    holder.tvTopicText.setText(realm.getName());

    holder.rootView.setOnClickListener(
        v -> onCategoryClickListener.onCategoryClick(v, realm.getId(), realm.getName()));
  }

  @Override
  public void unbind(CategoryHolder holder) {
    holder.rootView.setOnClickListener(null);
  }

  static class CategoryHolder extends BaseEpoxyHolder {
    @BindView(R.id.fl_topic_root)
    ViewGroup rootView;

    @BindView(R.id.iv_category_bg)
    ImageView ivTopicBackground;

    @BindView(R.id.tv_category_text)
    TextView tvTopicText;
  }
}