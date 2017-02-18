package com.yoloo.android.feature.chat.conversationlist;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import timber.log.Timber;

public class ConversationListController
    extends MvpController<ConversationListView, ConversationListPresenter>
    implements ConversationListView, OnItemClickListener<Chat>,
    OnItemLongClickListener<Chat> {

  @BindView(R.id.rv_chat) RecyclerView rvChat;
  @BindView(R.id.toolbar_chat) Toolbar toolbar;

  private ConversationListAdapter adapter;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_conversation, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onChatAdded(Chat chat) {
    adapter.addChat(chat);
  }

  @Override public void onChatChanged(Chat chat) {
    adapter.changeChat(chat);
  }

  @Override public void onChatRemoved(Chat chat) {
    adapter.removeChat(chat);
  }

  @Override public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull @Override public ConversationListPresenter createPresenter() {
    return new ConversationListPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        ChatRepository.getInstance());
  }

  @OnClick(R.id.fab_start_chat) void startConversation() {

  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, Chat item) {

  }

  @Override public void onItemLongClick(View v, EpoxyModel<?> model, Chat item) {
    // TODO: 10.02.2017 Show a context dialog here.
    adapter.removeChat(model);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_toolbar_conversationlist_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupRecyclerView() {
    adapter = new ConversationListAdapter(getActivity(), this, this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

    rvChat.setLayoutManager(layoutManager);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvChat.setItemAnimator(animator);
    rvChat.addItemDecoration(new DividerItemDecoration(getActivity(), OrientationHelper.VERTICAL));

    rvChat.setHasFixedSize(true);
    rvChat.setAdapter(adapter);
  }
}
