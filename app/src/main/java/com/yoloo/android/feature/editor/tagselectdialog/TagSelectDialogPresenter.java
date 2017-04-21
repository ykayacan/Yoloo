package com.yoloo.android.feature.editor.tagselectdialog;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import timber.log.Timber;

class TagSelectDialogPresenter extends MvpPresenter<TagSelectDialogView> {

  private final TagRepository tagRepository;

  TagSelectDialogPresenter(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Override
  public void onAttachView(TagSelectDialogView view) {
    super.onAttachView(view);
    loadRecommendedTags();
  }

  private void loadRecommendedTags() {
    List<TagRealm> tags = new ArrayList<>();
    TagRealm tag1 = new TagRealm().setId(UUID.randomUUID().toString()).setName("Accommodation");
    TagRealm tag2 = new TagRealm().setId(UUID.randomUUID().toString()).setName("Test2");
    TagRealm tag3 = new TagRealm().setId(UUID.randomUUID().toString()).setName("Test3");
    TagRealm tag4 = new TagRealm().setId(UUID.randomUUID().toString()).setName("Test4");
    TagRealm tag5 = new TagRealm().setId(UUID.randomUUID().toString()).setName("Test5");

    tags.add(tag1);
    tags.add(tag2);
    tags.add(tag3);
    tags.add(tag4);
    tags.add(tag5);

    getView().onRecommendedTagsLoaded(tags);

    /*Disposable d = tagRepository
        .listRecommendedTags()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tags -> getView().onRecommendedTagsLoaded(tags), Timber::e);

    getDisposable().add(d);*/
  }

  void searchTag(@Nonnull String query) {
    Disposable d = tagRepository
        .searchTag(query, null, 8)
        .observeOn(AndroidSchedulers.mainThread())
        .map(Response::getData)
        .subscribe(tags -> getView().onSearchTags(tags), Timber::e);

    getDisposable().add(d);
  }
}
