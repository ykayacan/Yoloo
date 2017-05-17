package com.yoloo.android.feature.search.tag;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.feature.search.TagModel_;
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
        .onClickListener(v -> onTagClickListener.onItemClick(v, tag))
        .addTo(this);
  }
}
