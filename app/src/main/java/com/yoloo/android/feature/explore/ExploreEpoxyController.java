package com.yoloo.android.feature.explore;

import android.view.View;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.feature.explore.data.ButtonItem;
import com.yoloo.android.feature.explore.data.ExploreItem;
import com.yoloo.android.feature.explore.data.GroupItem;
import com.yoloo.android.feature.explore.data.RecentMediaListItem;
import com.yoloo.android.feature.explore.model.ExploreButtonModel_;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController$GroupListItemModel_;
import com.yoloo.android.feature.models.recentmedias.RecentMediaListModelGroup;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.ArrayList;
import java.util.List;

public class ExploreEpoxyController extends Typed2EpoxyController<List<ExploreItem<?>>, Void> {

  private final RequestManager glide;

  @AutoModel ExploreButtonModel_ exploreButtonModel;

  private List<ExploreItem<?>> items;

  private RecentMediaListModelGroup.RecentPhotosModelGroupCallbacks recentPhotosModelGroupCallbacks;

  private View.OnClickListener onNewClickListener;
  private View.OnClickListener onTrendingClickListener;

  private OnItemClickListener<GroupRealm> onGroupClickListener;
  private GroupListEpoxyController.OnSubscribeListener onSubscribeClickListener;

  public ExploreEpoxyController(RequestManager glide) {
    this.glide = glide;
    this.items = new ArrayList<>();
  }

  public void setRecentPhotosModelGroupCallbacks(
      RecentMediaListModelGroup.RecentPhotosModelGroupCallbacks callbacks) {
    this.recentPhotosModelGroupCallbacks = callbacks;
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

  public void setOnSubscribeClickListener(GroupListEpoxyController.OnSubscribeListener listener) {
    this.onSubscribeClickListener = listener;
  }

  public void updateItem(ExploreItem<GroupRealm> item) {

  }

  @Override
  public void setData(List<ExploreItem<?>> items, Void data2) {
    this.items = items;
    super.setData(items, data2);
  }

  @Override
  protected void buildModels(List<ExploreItem<?>> items, Void aVoid) {
    Stream.of(items).forEach(item -> {
      if (item instanceof RecentMediaListItem) {
        new RecentMediaListModelGroup((RecentMediaListItem) item, recentPhotosModelGroupCallbacks,
            glide).addIf(!((RecentMediaListItem) item).getItem().isEmpty(), this);
      } else if (item instanceof ButtonItem) {
        exploreButtonModel
            .onTrendingClickListener(onTrendingClickListener)
            .onNewClickListener(onNewClickListener)
            .addTo(this);
      } else if (item instanceof GroupItem) {
        createGroupListModel(((GroupItem) item).getItem());
      }
    });
  }

  private void createGroupListModel(GroupRealm group) {
    new GroupListEpoxyController$GroupListItemModel_()
        .id(group.getId())
        .group(group)
        .glide(glide)
        .onSubscribeClickListener(onSubscribeClickListener)
        .onItemClickListener(onGroupClickListener)
        .addTo(this);
  }
}
