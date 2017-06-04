package com.yoloo.android.feature.postdetail.mvi;

import android.support.annotation.NonNull;
import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;

public interface PostDetailMviView extends MvpView {

  /**
   * The intent to load the first page
   *
   * @return The emitted item boolean can be ignored because it is always true
   */
  @NonNull Observable<String> loadFirstPageIntent();

  /**
   * The intent to load the next page
   *
   * @return The emitted item boolean can be ignored because it is always true
   */
  @NonNull Observable<Boolean> loadNextPageIntent();

  /**
   * The intent to react on pull-to-refresh
   *
   * @return The emitted item boolean can be ignored because it is always true
   */
  @NonNull Observable<String> pullToRefreshIntent();

  @NonNull Observable<CommentRealm> newCommentIntent();

  @NonNull Observable<PostRealm> bookmarkIntent();

  @NonNull Observable<PostRealm> deletePostIntent();

  @NonNull Observable<CommentRealm> deleteCommentIntent();

  @NonNull Observable<Pair<PostRealm, Integer>> votePostIntent();

  @NonNull Observable<Pair<CommentRealm, Integer>> voteCommentIntent();

  @NonNull Observable<CommentRealm> acceptCommentIntent();

  /**
   * Renders the viewState
   */
  void render(PostDetailViewState viewState);
}
