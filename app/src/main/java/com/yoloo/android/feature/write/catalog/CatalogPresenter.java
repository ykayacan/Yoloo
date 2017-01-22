package com.yoloo.android.feature.write.catalog;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class CatalogPresenter extends MvpPresenter<CatalogView> {

  private final PostRepository postRepository;

  public CatalogPresenter(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Override public void onAttachView(CatalogView view) {
    super.onAttachView(view);
    Disposable d = postRepository.addOrGetDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }

  public void updateDraft(PostRealm draft) {
    Disposable d =
        postRepository.updateDraft(draft).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  public void deleteDraft() {
    Disposable d =
        postRepository.deleteDraft().observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }
}
