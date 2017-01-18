package com.yoloo.android.data.repository.comment.datasource;

import com.google.firebase.auth.FirebaseAuth;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import io.reactivex.Observable;
import java.util.List;

public class CommentRemoteDataStore {

  private static CommentRemoteDataStore INSTANCE;

  private CommentRemoteDataStore() {
  }

  public static CommentRemoteDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new CommentRemoteDataStore();
    }
    return INSTANCE;
  }

  public Observable<CommentRealm> get(String commentId) {
    FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(task -> {

    });
    return Observable.empty();
  }

  public Observable<CommentRealm> add(CommentRealm comment) {
    FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(task -> {

    });
    return Observable.just(comment);
  }

  public void delete(String commentId) {
    FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(task -> {

    });
  }

  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, String eTag,
      int limit) {
    return Observable.empty();
  }
}
