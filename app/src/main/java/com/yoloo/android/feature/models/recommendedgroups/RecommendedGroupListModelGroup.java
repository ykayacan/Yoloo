package com.yoloo.android.feature.models.recommendedgroups;

import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.feed.RecommendedGroupListFeedItem;
import java.util.ArrayList;
import java.util.List;

public class RecommendedGroupListModelGroup extends EpoxyModelGroup {

  public RecommendedGroupListModelGroup(RecommendedGroupListFeedItem item,
      Callbacks callbacks, RequestManager glide) {
    super(R.layout.item_recommended_group_list, buildModels(item, callbacks, glide));
    id(item.id());
  }

  private static List<EpoxyModel<?>> buildModels(RecommendedGroupListFeedItem item,
      Callbacks callbacks, RequestManager glide) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_header_text));

    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_more_text).onClick(
        v -> callbacks.onRecommendedGroupsHeaderClicked()));

    // inner group models
    List<RecommendedGroupModel_> groupModels = Stream
        .of(item.getItem())
        .map(group -> new RecommendedGroupModel_()
            .id(group.getId())
            .glide(glide)
            .groupImageUrl(group.getImageWithIconUrl())
            .groupTitle(group.getName())
            .roundCorners(6.0F)
            .imageQualityModifier("150")
            .onClickListener(v -> callbacks.onRecommendedGroupClicked(group)))
        .toList();

    // inner recyclerview
    models.add(new RecommendedGroupListModel_().numItemsExpectedOnDisplay(5).models(groupModels));

    return models;
  }

  public interface Callbacks {
    void onRecommendedGroupsHeaderClicked();

    void onRecommendedGroupClicked(GroupRealm group);
  }
}
