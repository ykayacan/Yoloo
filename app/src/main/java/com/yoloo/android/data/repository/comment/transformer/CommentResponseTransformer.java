package com.yoloo.android.data.repository.comment.transformer;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseComment;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class CommentResponseTransformer
    implements ObservableTransformer<CollectionResponseComment, Response<List<CommentRealm>>> {

  public static CommentResponseTransformer create() {
    return new CommentResponseTransformer();
  }

  @Override public ObservableSource<Response<List<CommentRealm>>> apply(
      Observable<CollectionResponseComment> upstream) {
    return upstream.map(response -> {
      if (response.getItems() == null) {
        return Response.create(Collections.emptyList(), null);
      } else {
        return Response.create(
            Stream.of(response.getItems()).map(CommentRealm::new).toList(),
            response.getNextPageToken());
      }
    });
  }
}
