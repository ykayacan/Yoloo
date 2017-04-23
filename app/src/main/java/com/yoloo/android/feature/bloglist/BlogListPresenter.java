package com.yoloo.android.feature.bloglist;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class BlogListPresenter extends MvpPresenter<BlogListView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;
  private int limit = 20;

  BlogListPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(BlogListView view) {
    super.onAttachView(view);
    loadTrendingBlogs();
  }

  void refreshBlogData() {
    cursor = null;

    Disposable d = getTrendingBlogsObservable().subscribe(response -> {
      cursor = response.getCursor();
      getView().onLoaded(response.getData());
    }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private void loadTrendingBlogs() {
    getView().onLoading(false);

    Observable<AccountRealm> meObservable =
        userRepository.getLocalMe().observeOn(AndroidSchedulers.mainThread()).toObservable();

    Observable<Response<List<PostRealm>>> trendingBlogsObservable = getTrendingBlogsObservable();

    Disposable d =
        Observable.zip(meObservable, trendingBlogsObservable, Pair::create).subscribe(pair -> {
          getView().onMeLoaded(pair.first);
          Response<List<PostRealm>> response = pair.second;

          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private Observable<Response<List<PostRealm>>> getTrendingBlogsObservable() {
    return postRepository.listByTrendingBlogPosts(cursor, limit)
        .observeOn(AndroidSchedulers.mainThread());
  }
}
