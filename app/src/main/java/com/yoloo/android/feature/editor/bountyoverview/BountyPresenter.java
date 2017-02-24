package com.yoloo.android.feature.editor.bountyoverview;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BountyPresenter extends MvpPresenter<BountyView> {

  private final UserRepository userRepository;
  private final PostRepository postRepository;

  public BountyPresenter(UserRepository userRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
  }

  @Override public void onAttachView(BountyView view) {
    super.onAttachView(view);
    loadDraftAndUser();
  }

  private void loadDraftAndUser() {
    Disposable d = Observable.zip(postRepository.getDraft(), userRepository.getLocalMe(),
        Pair::create)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(pair -> getView().onDraftAndAccountLoaded(pair.first, pair.second),
            this::showError);

    getDisposable().add(d);
  }

  public void updateDraft(PostRealm draft) {
    Disposable d = postRepository.addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved());

    getDisposable().add(d);
  }

  public void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
