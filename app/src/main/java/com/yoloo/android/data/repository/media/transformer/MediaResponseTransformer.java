package com.yoloo.android.data.repository.media.transformer;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseMedia;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class MediaResponseTransformer
    implements ObservableTransformer<CollectionResponseMedia, Response<List<MediaRealm>>> {

  public static MediaResponseTransformer create() {
    return new MediaResponseTransformer();
  }

  @Override public ObservableSource<Response<List<MediaRealm>>> apply(
      Observable<CollectionResponseMedia> upstream) {
    return upstream.map(response -> Response.create(
        Stream.of(response.getItems()).map(MediaRealm::new).toList(),
        response.getNextPageToken()));
  }
}
