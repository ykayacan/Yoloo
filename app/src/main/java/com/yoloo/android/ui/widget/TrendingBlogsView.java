package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.SimpleEpoxyController;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

public class TrendingBlogsView extends RecyclerView {

  private final LinearLayoutManager layoutManager;
  private SimpleEpoxyController controller;

  public TrendingBlogsView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setHasFixedSize(true);

    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    setLayoutManager(layoutManager);

    setOnFlingListener(null);
    new LinearSnapHelper().attachToRecyclerView(this);

    addItemDecoration(new SpaceItemDecoration(4, SpaceItemDecoration.HORIZONTAL));
  }

  public void setInitialPrefetchItemCount(int count) {
    layoutManager.setInitialPrefetchItemCount(count);
  }

  public void setModels(List<? extends EpoxyModel<?>> models) {
    // If this is the first time setting models we create a new controller. This is because the
    // first time a controller builds models it happens right away, otherwise it is posted. We
    // need it to happen right away so the models show immediately and so the adapter is
    // populated so the carousel scroll state can be restored.
    if (controller == null) {
      controller = new SimpleEpoxyController();
      setAdapter(controller.getAdapter());
    }

    // If the models are set again without being cleared first (eg colors are inserted, shuffled,
    // or changed), then reusing the same controller allows diffing to work correctly.
    controller.setModels(models);
  }

  public void clearModels() {
    // The controller is cleared so the next time models are set we can create a fresh one.
    controller.cancelPendingModelBuild();
    controller = null;

    // We use swapAdapter instead of setAdapter so that the view pool is not cleared.
    // 'removeAndRecycleExistingViews=true' is used
    // since the carousel is going off screen and these views can now be recycled to be used in
    // another carousel (assuming there is a shared view pool)
    swapAdapter(null, true);
  }
}
