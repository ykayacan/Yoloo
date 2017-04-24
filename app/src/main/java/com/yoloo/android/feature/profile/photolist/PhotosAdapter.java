package com.yoloo.android.feature.profile.photolist;

import android.widget.ImageView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import com.yoloo.android.util.VersionUtil;

import java.util.List;

import butterknife.BindView;

class PhotosAdapter extends SelectableAdapter {

  private final OnItemClickListener<MediaRealm> onItemClickListener;

  PhotosAdapter(OnItemClickListener<MediaRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
    enableDiffing();
  }

  void addMedias(List<MediaRealm> medias) {
    models.clear();

    List<PhotosAdapter$PhotosModel_> photosModels = Stream.of(medias)
        .map(media -> new PhotosAdapter$PhotosModel_()
            .adapter(this)
            .media(media)
            .onItemClickListener(onItemClickListener))
        .toList();

    models.addAll(photosModels);
    notifyModelsChanged();
  }

  List<MediaRealm> getSelectedMedia() {
    return Stream.of(getSelectedItems())
        .select(PhotosModel.class)
        .map(PhotosModel::getMedia)
        .toList();
  }

  @EpoxyModelClass(layout = R.layout.item_photo_grid)
  public abstract static class PhotosModel extends EpoxyModelWithHolder<PhotosModel
      .PhotosHolder> {

    @EpoxyAttribute MediaRealm media;
    @EpoxyAttribute(hash = false) OnItemClickListener<MediaRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) PhotosAdapter adapter;

    @Override public void bind(PhotosHolder holder) {
      Glide.with(holder.itemView.getContext())
          .load(media.getMediumSizeUrl())
          .into(holder.ivPhoto);

      if (VersionUtil.hasL()) {
        holder.ivPhoto.setTransitionName("image.test");
      }

      final boolean isSelected = adapter.isSelected(this);
      holder.ivPhoto.setSelected(isSelected);

      holder.itemView.setOnClickListener(v -> {
        adapter.toggleSelection(this);
        onItemClickListener.onItemClick(v, this, media);
      });
    }

    MediaRealm getMedia() {
      return media;
    }

    static class PhotosHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_photos_grid) ImageView ivPhoto;
    }
  }
}
