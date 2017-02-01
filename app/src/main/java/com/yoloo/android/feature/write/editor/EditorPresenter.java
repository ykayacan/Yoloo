package com.yoloo.android.feature.write.editor;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class EditorPresenter extends MvpPresenter<EditorView> {

  public static final int NAV_BACK = -1;
  public static final int NAV_BOUNTY = 1;
  public static final int NAV_POST = 2;

  private final TagRepository tagRepository;
  private final PostRepository postRepository;

  public EditorPresenter(TagRepository tagRepository, PostRepository postRepository) {
    this.tagRepository = tagRepository;
    this.postRepository = postRepository;
  }

  @Override public void onAttachView(EditorView view) {
    super.onAttachView(view);
    loadDraft();
  }

  public void loadDraft() {
    Disposable d = postRepository.getDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }

  public void updateDraft(PostRealm draft, int navigation) {
    Disposable d = postRepository.addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved(navigation));

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
