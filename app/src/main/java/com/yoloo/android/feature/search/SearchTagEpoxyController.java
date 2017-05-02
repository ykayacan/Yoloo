package com.yoloo.android.feature.search;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class SearchTagEpoxyController extends TypedEpoxyController<List<TagRealm>> {

  private final OnItemClickListener<TagRealm> onTagClickListener;

  SearchTagEpoxyController(OnItemClickListener<TagRealm> onTagClickListener) {
    this.onTagClickListener = onTagClickListener;
  }

  @Override
  protected void buildModels(List<TagRealm> tags) {
    Stream.of(tags).forEach(this::createTagModel);
  }

  private void createTagModel(TagRealm tag) {
    new TagModel_()
        .id(tag.getId())
        .tag(tag)
        .onClickListener(v -> onTagClickListener.onItemClick(v, null, tag))
        .addTo(this);
  }
}
