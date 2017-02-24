package com.yoloo.android.feature.editor.catalog;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class CatalogPresenter extends MvpPresenter<CatalogView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public CatalogPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(CatalogView view) {
    super.onAttachView(view);
    createDraft();
  }

  private void createDraft() {
    Disposable d = userRepository.getLocalMe()
        .map(this::createNewDraft)
        .flatMapCompletable(postRepository::addDraft)
        .andThen(postRepository.getDraft())
        .onErrorResumeNext(throwable -> {
          return postRepository.getDraft();
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft), Timber::e);

    getDisposable().add(d);
  }

  public void loadDraft() {
    Disposable d = postRepository.getDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }

  public void updateDraft(PostRealm draft) {
    Disposable d = postRepository.addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved());

    getDisposable().add(d);
  }

  public void deleteDraft() {
    Disposable d = postRepository.deleteDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private PostRealm createNewDraft(AccountRealm account) {
    return new PostRealm()
        .setId("draft")
        .setOwnerId(account.getId())
        .setAvatarUrl(account.getAvatarUrl())
        .setUsername(account.getUsername())
        .setDir(0)
        .setPending(true);
  }
}
