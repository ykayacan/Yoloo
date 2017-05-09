package com.yoloo.android.feature.editor.selectbounty;

import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class BountySelectPresenter extends MvpPresenter<BountySelectView> {

  private final UserRepository userRepository;
  private final PostRepository postRepository;

  BountySelectPresenter(UserRepository userRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
  }

  @Override
  public void onAttachView(BountySelectView view) {
    super.onAttachView(view);
    loadDraftAndUser();
  }

  private void loadDraftAndUser() {
    Disposable d = Single
        .zip(postRepository.getDraft(), userRepository.getMe().singleOrError(), Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> getView().onDraftAndAccountLoaded(pair.first, pair.second),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void updateDraft(PostRealm draft) {
    Disposable d = postRepository
        .addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved());

    getDisposable().add(d);
  }
}
