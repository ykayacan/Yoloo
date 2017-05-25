package com.yoloo.android.feature.bloglist;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class BlogListPresenter extends MvpPresenter<BlogListView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;

  BlogListPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(BlogListView view) {
    super.onAttachView(view);
    loadTrendingBlogs();
  }

  void loadTrendingBlogs() {
    getView().onLoading(false);

    Disposable d = getTrendingBlogsObservable()
            .subscribe(response -> {
              cursor = response.getCursor();

              getView().onLoaded(new ArrayList<>(mapPostsToFeedItems(response.getData())));
            }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  void deletePost(String postId) {
    Disposable d = postRepository
        .deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Bookmark post.
   *
   * @param postId the post id
   */
  void bookmarkPost(String postId) {
    Disposable d = postRepository
        .bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Un bookmark post.
   *
   * @param postId the post id
   */
  void unBookmarkPost(String postId) {
    Disposable d = postRepository
        .unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Vote post.
   *
   * @param postId the post id
   * @param direction the direction
   */
  void votePost(String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  private Observable<Response<List<PostRealm>>> getTrendingBlogsObservable() {
    return postRepository.listByTrendingBlogPosts(cursor, 20)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private List<BlogPostFeedItem> mapPostsToFeedItems(List<PostRealm> data) {
    return Stream.of(data).map(BlogPostFeedItem::new).toList();
  }
}
