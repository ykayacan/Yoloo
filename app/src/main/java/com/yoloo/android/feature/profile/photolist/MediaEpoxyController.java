package com.yoloo.android.feature.profile.photolist;

import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class MediaEpoxyController extends Typed2EpoxyController<List<MediaRealm>, Boolean> {

  private final OnItemClickListener<MediaRealm> onItemClickListener;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  MediaEpoxyController(OnItemClickListener<MediaRealm> onItemClickListener, RequestManager glide) {
    this.onItemClickListener = onItemClickListener;
    this.glide = glide;
  }

  @Override
  protected void buildModels(List<MediaRealm> medias, Boolean loadingMore) {
    Stream.of(medias).forEach(this::createMediaModel);

    loaderModel.addIf(loadingMore, this);
  }

  private void createMediaModel(MediaRealm media) {
    new MediaModel_()
        .id(media.getId())
        .glide(glide)
        .onClickListener(v -> onItemClickListener.onItemClick(null, media))
        .addTo(this);
  }
}
