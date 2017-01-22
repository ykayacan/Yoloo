package com.yoloo.android.feature.feed.bountyfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class BountyFeedPresenter extends MvpPresenter<BountyFeedView> {

  private final PostRepository postRepository;

  public BountyFeedPresenter(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Override public void onAttachView(BountyFeedView view) {
    super.onAttachView(view);
    loadBountyPosts(false, null, null, 20);
  }

  public void loadBountyPosts(boolean pullToRefresh, String cursor, String eTag, int limit) {
    Disposable d = postRepository.list(cursor, eTag, limit, PostSorter.BOUNTY, null)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showFeed, this::showError);

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  public void deletePost(String postId) {
    Disposable d =
        postRepository.delete(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  public void vote(String postId, int direction) {
    Disposable d = postRepository.vote(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.get(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

    getDisposable().add(d);
  }

  private void showFeed(Response<List<PostRealm>> response) {
    getView().onLoaded(response);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
