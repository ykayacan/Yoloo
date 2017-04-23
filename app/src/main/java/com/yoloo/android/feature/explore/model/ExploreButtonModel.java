package com.yoloo.android.feature.explore.model;

import android.support.v7.widget.CardView;
import android.view.View;
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

    holder.cardTrending.setOnClickListener(v -> onTrendingClickListener.onClick(v));
    holder.cardNew.setOnClickListener(v -> onNewClickListener.onClick(v));
  }

  static class ExploreButtonHolder extends BaseEpoxyHolder {
    @BindView(R.id.card_explore_trending) CardView cardTrending;
    @BindView(R.id.card_explore_new) CardView cardNew;
  }
}
