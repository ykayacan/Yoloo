package com.yoloo.android.feature.models.recentmedias;

import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.explore.data.RecentMediaListItem;
import java.util.ArrayList;
import java.util.List;

public class RecentMediaListModelGroup extends EpoxyModelGroup {

  public RecentMediaListModelGroup(RecentMediaListItem item,
      RecentPhotosModelGroupCallbacks callbacks, RequestManager glide) {
    super(R.layout.item_recommended_group_list, buildModels(item, callbacks, glide));
    id(item.getClass().getName());
  }

  private static List<EpoxyModel<?>> buildModels(RecentMediaListItem item,
      RecentPhotosModelGroupCallbacks callbacks, RequestManager glide) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_recent_medias_header_text));
    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_more_text).onClick(
        v -> callbacks.onRecentPhotosHeaderClicked()));

    // inner group models
    List<RecentMediaModel_> recentPhotoModels = Stream
        .of(item.getItem())
        .map(post -> new RecentMediaModel_()
            .post(post)
            .id(post.getId())
            .glide(glide)
            .onClickListener(v -> callbacks.onRecentPhotosClicked(post)))
        .toList();

    // inner recyclerview
    models.add(new RecentMediaListModel_().numItemsExpectedOnDisplay(5).models(recentPhotoModels));

    return models;
  }

  public interface RecentPhotosModelGroupCallbacks {
    void onRecentPhotosHeaderClicked();

    void onRecentPhotosClicked(PostRealm post);
  }
}
