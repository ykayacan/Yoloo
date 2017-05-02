package com.yoloo.android.feature.models.newusers;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelWithView;
import com.yoloo.android.ui.widget.NewUsersView;
import java.util.List;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class NewUserListModel extends EpoxyModelWithView<NewUsersView> {

  @EpoxyAttribute List<? extends EpoxyModel<?>> models;
  @EpoxyAttribute int numItemsExpectedOnDisplay;
  @EpoxyAttribute(DoNotHash) RecyclerView.RecycledViewPool recycledViewPool;

  @Override
  public void bind(NewUsersView view) {
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
  public void unbind(NewUsersView view) {
    super.unbind(view);
    view.clearModels();
  }

  @Override
  protected NewUsersView buildView(ViewGroup parent) {
    return new NewUsersView(parent.getContext(), null);
  }

  @Override
  public boolean shouldSaveViewState() {
    // Save the state of the scroll position
    return true;
  }
}
