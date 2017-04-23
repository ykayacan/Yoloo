package com.yoloo.android.feature.explore;

import android.content.Context;
import android.view.View;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.feature.explore.model.ExploreButtonModel_;
import com.yoloo.android.feature.explore.model.RecentMediaModel_;
import com.yoloo.android.feature.grouplist.GroupListAdapter;
import com.yoloo.android.feature.grouplist.GroupListAdapter$GroupListItemModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class ExploreAdapter extends EpoxyAdapter {

  private final Context context;
  private final RequestManager glide;

  private View.OnClickListener onRecentMediaHeaderClickListener;
  private OnItemClickListener<MediaRealm> onMediaClickListener;

  private View.OnClickListener onNewClickListener;
  private View.OnClickListener onTrendingClickListener;

  private OnItemClickListener<GroupRealm> onGroupClickListener;
  private GroupListAdapter.OnSubscribeListener onSubscribeClickListener;

  public ExploreAdapter(Context context, RequestManager glide) {
    this.context = context;
    this.glide = glide;
  }

  public void setOnRecentMediaHeaderClickListener(View.OnClickListener listener) {
    this.onRecentMediaHeaderClickListener = listener;
  }

  public void setOnMediaClickListener(OnItemClickListener<MediaRealm> listener) {
    this.onMediaClickListener = listener;
  }

  public void setOnNewClickListener(View.OnClickListener listener) {
    this.onNewClickListener = listener;
  }

  public void setOnTrendingClickListener(View.OnClickListener listener) {
    this.onTrendingClickListener = listener;
  }

  public void setOnGroupClickListener(OnItemClickListener<GroupRealm> listener) {
    this.onGroupClickListener = listener;
  }

  public void setOnSubscribeClickListener(GroupListAdapter.OnSubscribeListener listener) {
    this.onSubscribeClickListener = listener;
  }

  public void addItems(List<? extends ExploreItem<?>> items) {
    for (ExploreItem<?> item : items) {
      if (item instanceof RecentMediaItem) {
        addModel(createRecentMediaModel(((RecentMediaItem) item).getItem()));
      } else if (item instanceof ButtonItem) {
        addModel(createButtonModel());
        addModel(new SimpleEpoxyModel(R.layout.item_group_header));
      } else if (item instanceof GroupItem) {
        addModel(createGroupListModel(((GroupItem) item).getItem()));
      }
    }
  }

  public void updateItem(ExploreItem<GroupRealm> item) {
    for (EpoxyModel<?> model : models) {
      if (model instanceof GroupListAdapter$GroupListItemModel_) {
        if (item
            .getItem()
            .getId()
            .equals(((GroupListAdapter$GroupListItemModel_) model).getGroup().getId())) {
          notifyModelChanged(model, item);
        }
      }
    }
  }

  private RecentMediaModel_ createRecentMediaModel(List<MediaRealm> medias) {
    return new RecentMediaModel_(context, glide)
        .medias(medias)
        .onHeaderClickListener(onRecentMediaHeaderClickListener)
        .onItemClickListener(onMediaClickListener)
        .show(!medias.isEmpty());
  }

  private ExploreButtonModel_ createButtonModel() {
    return new ExploreButtonModel_()
        .onNewClickListener(onNewClickListener)
        .onTrendingClickListener(onTrendingClickListener);
  }

  private GroupListAdapter$GroupListItemModel_ createGroupListModel(GroupRealm group) {
    return new GroupListAdapter$GroupListItemModel_()
        .group(group)
        .glide(glide)
        .onSubscribeClickListener(onSubscribeClickListener)
        .onItemClickListener(onGroupClickListener);
  }
}
