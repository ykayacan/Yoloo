package com.yoloo.android.feature.search;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class SearchAdapter extends EpoxyAdapter {

  private final OnItemClickListener<TagRealm> onTagClickListener;
  private final OnProfileClickListener onProfileClickListener;
  private final OnFollowClickListener onFollowClickListener;

  private final CropCircleTransformation circleTransformation;

  SearchAdapter(Context context, OnItemClickListener<TagRealm> onTagClickListener,
      OnProfileClickListener onProfileClickListener, OnFollowClickListener onFollowClickListener) {
    this.onTagClickListener = onTagClickListener;
    this.onProfileClickListener = onProfileClickListener;
    this.onFollowClickListener = onFollowClickListener;

    enableDiffing();

    circleTransformation = new CropCircleTransformation(context);
  }

  void replaceTags(List<TagRealm> tags) {
    models.clear();

    models.addAll(Stream
        .of(tags)
        .map(tag -> new TagModel_().tag(tag).onTagClickListener(onTagClickListener))
        .collect(Collectors.toList()));

    notifyModelsChanged();
  }

  void replaceUsers(List<AccountRealm> accounts) {
    models.clear();

    models.addAll(Stream
        .of(accounts)
        .map(account -> new UserModel_()
            .account(account)
            .cropCircleTransformation(circleTransformation)
            .onProfileClickListener(onProfileClickListener)
            .onFollowClickListener(onFollowClickListener))
        .collect(Collectors.toList()));

    notifyModelsChanged();
  }
}
