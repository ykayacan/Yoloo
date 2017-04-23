package com.yoloo.android.data.repository.post;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponsePostEntity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class PostResponseTransformer
    implements ObservableTransformer<CollectionResponsePostEntity, Response<List<PostRealm>>> {

  private PostResponseTransformer() {
    // empty constructor
  }

  public static PostResponseTransformer create() {
    return new PostResponseTransformer();
  }

  @Override
  public ObservableSource<Response<List<PostRealm>>> apply(
      Observable<CollectionResponsePostEntity> upstream) {
    return upstream.map(
        response -> Response.create(getPosts(response), response.getNextPageToken()));
  }

  private List<PostRealm> getPosts(CollectionResponsePostEntity response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(PostRealm::new).toList();
  }
}
