package com.yoloo.android.feature.editor.editor;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.editor.job.SendPostJob;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.Date;
import javax.annotation.Nonnull;
import timber.log.Timber;

class EditorQuestionPresenter extends MvpPresenter<EditorQuestionView> {

  static final int NAV_SELECT_GROUP = 0;
  static final int NAV_BOUNTY = 1;
  static final int NAV_POST = 2;
  static final int NAV_SEND = 3;

  private final TagRepository tagRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  EditorQuestionPresenter(TagRepository tagRepository, PostRepository postRepository,
      UserRepository userRepository) {
    this.tagRepository = tagRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void onAttachView(EditorQuestionView view) {
    super.onAttachView(view);
    createDraft();
    loadRecommendedTags();
  }

  private void createDraft() {
    Disposable d = userRepository.getLocalMe()
        .map(this::createNewDraft)
        .flatMapCompletable(postRepository::addDraft)
        .andThen(postRepository.getDraft())
        .onErrorResumeNext(throwable -> postRepository.getDraft())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftCreated(draft), Timber::e);

    getDisposable().add(d);
  }

  /*void loadDraft() {
    Disposable d = postRepository
        .getDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }*/

  void deleteDraft() {
    Disposable d = postRepository.deleteDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void updateDraft(@Nonnull PostRealm draft, int navigation) {
    Disposable d = postRepository
        .addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftUpdated(navigation), Timber::e);

    getDisposable().add(d);
  }

  void sendPost() {
    SendPostJob.scheduleJob();
  }

  void searchTag(@Nonnull String query) {
    Disposable d = tagRepository
        .searchTag(query, null, 8)
        .observeOn(AndroidSchedulers.mainThread())
        .map(Response::getData)
        .subscribe(tags -> getView().onSearchTags(tags), Timber::e);

    getDisposable().add(d);
  }

  private PostRealm createNewDraft(AccountRealm account) {
    return new PostRealm()
        .setId("draft")
        .setOwnerId(account.getId())
        .setAvatarUrl(account.getAvatarUrl())
        .setUsername(account.getUsername())
        .setVoteDir(0)
        .setCreated(new Date())
        .setPending(true);
  }

  private void loadRecommendedTags() {
    Disposable d = tagRepository
        .listRecommendedTags()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tags -> getView().onRecommendedTagsLoaded(tags), Timber::e);

    getDisposable().add(d);
  }
}
