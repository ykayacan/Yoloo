package com.yoloo.android.feature.editor.editorcategorylist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;
import timber.log.Timber;

class EditorCategoryListPresenter extends MvpPresenter<EditorCategoryListView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  EditorCategoryListPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(EditorCategoryListView view) {
    super.onAttachView(view);
    createDraft();
  }

  private void createDraft() {
    Disposable d = userRepository.getLocalMe()
        .map(this::createNewDraft)
        .flatMapCompletable(postRepository::addDraft)
        .andThen(postRepository.getDraft())
        .onErrorResumeNext(throwable -> postRepository.getDraft())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft), Timber::e);

    getDisposable().add(d);
  }

  void loadDraft() {
    Disposable d = postRepository.getDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }

  void updateDraft(@Nonnull PostRealm draft) {
    Disposable d = postRepository.addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved());

    getDisposable().add(d);
  }

  void deleteDraft() {
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
        .setVoteDir(0)
        .setPending(true);
  }
}
