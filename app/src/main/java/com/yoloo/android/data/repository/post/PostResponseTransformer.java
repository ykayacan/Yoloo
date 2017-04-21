package com.yoloo.android.data.repository.post;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponsePost;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class PostResponseTransformer
    implements ObservableTransformer<CollectionResponsePost, Response<List<PostRealm>>> {

  private PostResponseTransformer() {
    // empty constructor
  }

  public static PostResponseTransformer create() {
    return new PostResponseTransformer();
  }

  @Override public ObservableSource<Response<List<PostRealm>>> apply(
      Observable<CollectionResponsePost> upstream) {
    return upstream.map(response -> Response.create(
        getPosts(response),
        response.getNextPageToken()));
  }

  private List<PostRealm> getPosts(CollectionResponsePost response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(PostRealm::new).toList();
  }
}
