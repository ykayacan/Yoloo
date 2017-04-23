package com.yoloo.android.feature.feed.component.newcomers;

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
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_newcomers)
public abstract class NewcomersModel extends EpoxyModelWithHolder<NewcomersModel.NewcomersHolder> {

  @EpoxyAttribute(hash = false) View.OnClickListener onHeaderClickListener;

  private NewcomersContactAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private LinearLayoutManager lm;
  private SnapHelper snapHelper;

  public NewcomersModel(Context context) {
    adapter = new NewcomersContactAdapter();
    itemDecoration = new SpaceItemDecoration(8, SpaceItemDecoration.HORIZONTAL);
    snapHelper = new LinearSnapHelper();
    lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    lm.setInitialPrefetchItemCount(4);
  }

  @Override
  public void bind(NewcomersHolder holder) {
    holder.rvNewcomers.removeItemDecoration(itemDecoration);
    holder.rvNewcomers.addItemDecoration(itemDecoration);

    holder.rvNewcomers.setLayoutManager(lm);
    holder.rvNewcomers.setHasFixedSize(true);

    holder.rvNewcomers.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.rvNewcomers);

    if (holder.rvNewcomers.getAdapter() == null) {
      holder.rvNewcomers.setAdapter(adapter);
    }

    if (onHeaderClickListener == null) {
      throw new IllegalStateException("onHeaderClickListener is null.");
    }

    holder.tvMore.setOnClickListener(v -> onHeaderClickListener.onClick(v));
  }

  @Override
  public void unbind(NewcomersHolder holder) {
    holder.tvMore.setOnClickListener(null);
  }

  public void addNewcomersContacts(List<AccountRealm> items) {
    adapter.addNewcomersContacts(items);
  }

  public void setOnFollowClickListener(OnFollowClickListener listener) {
    adapter.setOnFollowClickListener(listener);
  }

  public void setOnItemClickListener(OnItemClickListener<AccountRealm> listener) {
    adapter.setOnItemClickListener(listener);
  }

  public NewcomersContactAdapter getAdapter() {
    return adapter;
  }

  static class NewcomersHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_feed_newcomers_more) TextView tvMore;
    @BindView(R.id.rv_feed_newcomers) RecyclerView rvNewcomers;
  }
}
