package com.yoloo.android.feature.chat.createchat;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.chat.Dialog;
import com.yoloo.android.data.model.chat.User;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.chat.chat.ChatController;
import com.yoloo.android.feature.search.UserModel;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class CreateChatController extends BaseController
    implements OnSearchViewListener, UserModel.OnUserClickListener {

  private final PublishSubject<String> searchSubject = PublishSubject.create();

  @BindView(R.id.msv) MaterialSearchView msv;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;

  private CompositeDisposable disposable;

  private CreateChatEpoxyController epoxyController;

  private ChatRepository chatRepository;
  private UserRepository userRepository;

  public static CreateChatController create() {
    return new CreateChatController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_create_chat, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setHasOptionsMenu(true);
    disposable = new CompositeDisposable();

    setupToolbar();
    setupRecyclerview();

    chatRepository = ChatRepository.getInstance();
    userRepository = UserRepositoryProvider.getRepository();

    msv.setOnSearchViewListener(this);

    processShowNewUsers();

    processUserSearch();
  }

  private void processShowNewUsers() {
    Disposable d = userRepository
        .listNewUsers(null, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> epoxyController.setData(response.getData()));

    disposable.add(d);
  }

  private void processUserSearch() {
    Disposable d = searchSubject
        .filter(s -> !TextUtils.isEmpty(s))
        .debounce(400, TimeUnit.MILLISECONDS)
        .switchMap(s -> userRepository.searchUser(s, null, 50))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> epoxyController.setData(response.getData()), Timber::e);

    disposable.add(d);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.create_chat_title);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_create_chat, menu);

    DrawableHelper.create().withColor(getActivity(), android.R.color.white).applyTo(menu);

    MenuItem item = menu.findItem(R.id.action_search);
    msv.setMenuItem(item);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override
  public void onSearchViewShown() {

  }

  @Override
  public void onSearchViewClosed() {

  }

  @Override
  public boolean onQueryTextSubmit(String s) {
    return false;
  }

  @Override
  public void onQueryTextChange(String s) {
    searchSubject.onNext(s);
  }

  private void setupRecyclerview() {
    epoxyController =
        new CreateChatEpoxyController(new CropCircleTransformation(getActivity()), this);

    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(epoxyController.getAdapter());
  }

  @Override
  public void onUserClicked(AccountRealm account) {
    userRepository.getLocalMe().observeOn(AndroidSchedulers.mainThread()).subscribe(me -> {
      if (me.getId().equals(account.getId())) {
        Snackbar
            .make(getView(), "You can't create a dialog with yourself", Snackbar.LENGTH_SHORT)
            .show();
        return;
      }

      User currentUser = new User(me.getId(), me.getUsername(), me.getAvatarUrl(), User.ROLE_ADMIN,
          account.getUsername(), account.getAvatarUrl());
      User targetUser =
          new User(account.getId(), account.getUsername(), account.getAvatarUrl(), User.ROLE_MEMBER,
              me.getUsername(), me.getAvatarUrl());

      KeyboardUtil.hideKeyboard(getView());

      HashMap<String, User> members = new HashMap<>(2);
      members.put(currentUser.getId(), currentUser);
      members.put(targetUser.getId(), targetUser);

      chatRepository
          .createDialog(
              new Dialog(targetUser.getAvatar(), targetUser.getName(), members, currentUser.getId(),
                  currentUser::getId))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(newDialog -> {
            Controller controller = ChatController.create(me.getId(), newDialog);

            getRouter().pushController(RouterTransaction
                .with(controller)
                .pushChangeHandler(new VerticalChangeHandler())
                .popChangeHandler(new VerticalChangeHandler()));
          });
    }, Timber::e);
  }
}
