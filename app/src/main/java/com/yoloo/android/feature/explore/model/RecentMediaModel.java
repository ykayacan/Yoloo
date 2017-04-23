package com.yoloo.android.feature.explore.model;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.feature.explore.SubRecentMediaAdapter;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_recent_medias)
public abstract class RecentMediaModel
    extends EpoxyModelWithHolder<RecentMediaModel.RecentMediasViewHolder> {

  @EpoxyAttribute List<MediaRealm> medias;
  @EpoxyAttribute(hash = false) View.OnClickListener onHeaderClickListener;
  @EpoxyAttribute(hash = false) OnItemClickListener<MediaRealm> onItemClickListener;

  private SubRecentMediaAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private LinearLayoutManager lm;
  private SnapHelper snapHelper;

  public RecentMediaModel(Context context, RequestManager glide) {
    adapter = new SubRecentMediaAdapter(glide);
    itemDecoration = new SpaceItemDecoration(4, SpaceItemDecoration.HORIZONTAL);
    snapHelper = new LinearSnapHelper();
    lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    lm.setInitialPrefetchItemCount(4);
  }

  @Override
  public void bind(RecentMediasViewHolder holder) {
    super.bind(holder);
    holder.recyclerView.removeItemDecoration(itemDecoration);
    holder.recyclerView.addItemDecoration(itemDecoration);

    holder.recyclerView.setLayoutManager(lm);
    holder.recyclerView.setHasFixedSize(true);

    holder.recyclerView.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.recyclerView);

    if (holder.recyclerView.getAdapter() == null) {
      holder.recyclerView.setAdapter(adapter);
      adapter.addItems(medias);
      adapter.setOnItemClickListener(onItemClickListener);
    }

    if (onHeaderClickListener == null) {
      throw new IllegalStateException("onHeaderClickListener is null.");
    }

    holder.tvMore.setOnClickListener(v -> onHeaderClickListener.onClick(v));
  }

  static class RecentMediasViewHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_explore_more) TextView tvMore;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
  }
}
