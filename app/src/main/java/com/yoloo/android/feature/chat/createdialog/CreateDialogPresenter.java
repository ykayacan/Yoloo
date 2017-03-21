package com.yoloo.android.feature.chat.createdialog;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class CreateDialogPresenter extends MvpPresenter<CreateDialogView> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;

  private String cursor;

  CreateDialogPresenter(UserRepository userRepository, ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
  }

  @Override public void onAttachView(CreateDialogView view) {
    super.onAttachView(view);
    loadMe();
  }

  private void loadMe() {
    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(me -> getView().onMeLoaded(me))
        .flatMapObservable(me -> userRepository.listFollowers(me.getId(), cursor, 40))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadFollowers(@Nonnull String userId) {
    Disposable d = userRepository.listFollowers(userId, cursor, 40)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  Chat createDialog(@Nonnull Chat chat) {
    return chatRepository.addDialog(chat);
  }

  void searchUsers(String query) {
    Disposable d = userRepository.searchUser(query, null, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showUsers, Timber::e);

    getDisposable().add(d);
  }

  private void showUsers(Response<List<AccountRealm>> response) {
    getView().onUsersLoaded(response.getData());
  }
}
