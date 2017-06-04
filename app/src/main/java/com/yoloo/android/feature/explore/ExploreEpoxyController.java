package com.yoloo.android.feature.explore;

import android.view.View;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.feature.explore.data.ButtonItem;
import com.yoloo.android.feature.explore.data.GroupItem;
import com.yoloo.android.feature.explore.data.RecentMediaListItem;
import com.yoloo.android.feature.explore.model.ExploreButtonModel_;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController$GroupListItemModel_;
import com.yoloo.android.feature.models.recentmedias.RecentMediaListModelGroup;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.Collection;

class ExploreEpoxyController extends TypedEpoxyController<Collection<FeedItem<?>>> {

  private final RequestManager glide;

  @AutoModel ExploreButtonModel_ exploreButtonModel;

  private RecentMediaListModelGroup.RecentPhotosModelGroupCallbacks recentPhotosModelGroupCallbacks;

  private View.OnClickListener onNewClickListener;
  private View.OnClickListener onTrendingClickListener;

  private OnItemClickListener<GroupRealm> onGroupClickListener;
  private GroupListEpoxyController.OnSubscribeListener onSubscribeClickListener;

  ExploreEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  void setRecentPhotosModelGroupCallbacks(
      RecentMediaListModelGroup.RecentPhotosModelGroupCallbacks callbacks) {
    this.recentPhotosModelGroupCallbacks = callbacks;
  }

  void setOnNewClickListener(View.OnClickListener listener) {
    this.onNewClickListener = listener;
  }

  void setOnTrendingClickListener(View.OnClickListener listener) {
    this.onTrendingClickListener = listener;
  }

  void setOnGroupClickListener(OnItemClickListener<GroupRealm> listener) {
    this.onGroupClickListener = listener;
  }

  void setOnSubscribeClickListener(GroupListEpoxyController.OnSubscribeListener listener) {
    this.onSubscribeClickListener = listener;
  }

  @Override
  protected void buildModels(Collection<FeedItem<?>> items) {
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
