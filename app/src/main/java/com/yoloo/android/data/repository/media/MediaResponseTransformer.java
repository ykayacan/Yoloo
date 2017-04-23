package com.yoloo.android.data.repository.media;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseMediaEntity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class MediaResponseTransformer
    implements ObservableTransformer<CollectionResponseMediaEntity, Response<List<MediaRealm>>> {

  public static MediaResponseTransformer create() {
    return new MediaResponseTransformer();
  }

  @Override
  public ObservableSource<Response<List<MediaRealm>>> apply(
      Observable<CollectionResponseMediaEntity> upstream) {
    return upstream.map(
        response -> Response.create(getMedias(response), response.getNextPageToken()));
  }

  private List<MediaRealm> getMedias(CollectionResponseMediaEntity response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(MediaRealm::new).toList();
  }
}
