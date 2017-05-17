package com.yoloo.android.feature.search.tag;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import timber.log.Timber;

class ChildTagSearchPresenter extends MvpPresenter<ChildTagSearchView> {

  private final TagRepository tagRepository;

  private String cursor;

  /**
   * Instantiates a new Child search presenter.
   *  @param tagRepository the tag repository
   *
   */
  ChildTagSearchPresenter(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  /**
   * Load recent tags.
   */
  void loadRecentTags() {
    Disposable d = tagRepository
        .listRecentTags()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentTags, Timber::e);

    getDisposable().add(d);
  }

  /**
   * Search tags.
   *
   * @param name the name
   * @param resetCursor the reset cursor
   */
  void searchTags(String name, boolean resetCursor) {
    if (resetCursor) {
      cursor = null;
    }

    Disposable d = tagRepository
        .searchTags(name, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showTags, Timber::e);

    getDisposable().add(d);
  }

  private void showRecentTags(List<TagRealm> tags) {
    getView().onRecentTagsLoaded(tags);
  }

  private void showTags(Response<List<TagRealm>> response) {
    cursor = response.getCursor();

    getView().onTagsLoaded(response.getData());
  }
}
