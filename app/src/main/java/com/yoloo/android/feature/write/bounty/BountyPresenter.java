package com.yoloo.android.feature.write.bounty;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
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
    loadTotalUserBounty();
    addOrGetDraft();
  }

  public void loadTotalUserBounty() {
    Disposable d = userRepository.getMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showTotalBounty, this::showError);

    getDisposable().add(d);
  }

  public void updateBounty(int bounty, int updateType) {
    if (updateType == 1) {
      showBountyRenewed(bounty);
    } else if (updateType == -1) {
      showBountyConsumed(bounty);
    }
  }

  public void addOrGetDraft() {
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

  private void showBountyConsumed(int bounty) {
    getView().onBountyConsumed(bounty);
  }

  private void showBountyRenewed(int bounty) {
    getView().onBountyRenewed(bounty);
  }

  public void showTotalBounty(AccountRealm account) {
    getView().onTotalBounty(account.getBounties());
  }

  public void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
