package com.yoloo.android.data.repository.tag.transformer;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseTag;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class TagResponseTransformer
    implements ObservableTransformer<CollectionResponseTag, Response<List<TagRealm>>> {

  private TagResponseTransformer() {
    // empty constructor
  }

  public static TagResponseTransformer create() {
    return new TagResponseTransformer();
  }

  @Override public ObservableSource<Response<List<TagRealm>>> apply(
      Observable<CollectionResponseTag> upstream) {
    return upstream.map(
        response -> Response.create(getTags(response), response.getNextPageToken()));
  }

  private List<TagRealm> getTags(CollectionResponseTag response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(TagRealm::new).toList();
  }
}
