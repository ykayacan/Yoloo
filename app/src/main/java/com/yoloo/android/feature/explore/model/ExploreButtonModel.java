package com.yoloo.android.feature.explore.model;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

@EpoxyModelClass(layout = R.layout.item_explore_button)
public abstract class ExploreButtonModel
    extends EpoxyModelWithHolder<ExploreButtonModel.ExploreButtonHolder> {

  @EpoxyAttribute(hash = false) View.OnClickListener onTrendingClickListener;
  @EpoxyAttribute(hash = false) View.OnClickListener onNewClickListener;

  @Override
  public void bind(ExploreButtonHolder holder) {
    super.bind(holder);

    holder.tvTrending.setOnClickListener(v -> onTrendingClickListener.onClick(v));
    holder.tvNew.setOnClickListener(v -> onNewClickListener.onClick(v));
  }

  static class ExploreButtonHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_explore_trending) TextView tvTrending;
    @BindView(R.id.tv_explore_new) TextView tvNew;
  }
}
