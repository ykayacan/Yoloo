package com.yoloo.android.feature.models.trendingblogs;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelWithView;
import com.yoloo.android.ui.widget.TrendingBlogsView;
import java.util.List;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class TrendingBlogListModel extends EpoxyModelWithView<TrendingBlogsView> {

  @EpoxyAttribute List<? extends EpoxyModel<?>> models;
  @EpoxyAttribute int numItemsExpectedOnDisplay;
  @EpoxyAttribute(DoNotHash) RecyclerView.RecycledViewPool recycledViewPool;

  @Override
  public void bind(TrendingBlogsView view) {
    super.bind(view);
    // If there are multiple carousels showing the same item types, you can benefit by having a
    // shared view pool between those carousels
    // so new views aren't created for each new carousel.
    if (recycledViewPool != null) {
      view.setRecycledViewPool(recycledViewPool);
    }

    if (numItemsExpectedOnDisplay != 0) {
      view.setInitialPrefetchItemCount(numItemsExpectedOnDisplay);
    }

    view.setModels(models);
  }

  @Override
  public void unbind(TrendingBlogsView view) {
    super.unbind(view);
    view.clearModels();
  }

  @Override
  protected TrendingBlogsView buildView(ViewGroup parent) {
    return new TrendingBlogsView(parent.getContext(), null);
  }

  @Override
  public boolean shouldSaveViewState() {
    // Save the state of the scroll position
    return true;
  }
}
