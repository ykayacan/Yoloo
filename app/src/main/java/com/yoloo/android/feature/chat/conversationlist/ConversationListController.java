package com.yoloo.android.feature.chat.conversationlist;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.chatkit.commons.ImageLoader;
import com.yoloo.android.chatkit.commons.models.IMessage;
import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.chatkit.dialogs.DialogsList;
import com.yoloo.android.chatkit.dialogs.DialogsListAdapter;
import com.yoloo.android.data.model.chat.DefaultDialog;
import com.yoloo.android.data.model.chat.DefaultUser;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpController;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class ConversationListController
    extends MvpController<ConversationListView, ConversationListPresenter>
    implements ConversationListView {

  @BindView(R.id.conversations_list) DialogsList dialogsList;
  @BindView(R.id.toolbar_chat) Toolbar toolbar;

  private ConversationListController() {}

  public static ConversationListController create() {
    return new ConversationListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_conversation_list, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
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

  }

  @Override public void onChatChanged(Chat chat) {

  }

  @Override public void onChatRemoved(Chat chat) {

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
    ImageLoader imageLoader = (imageView, url) -> Glide.with(getActivity())
        .load(url)
        .into(imageView);

    DialogsListAdapter dialogsListAdapter = new DialogsListAdapter(imageLoader);
    dialogsList.setAdapter(dialogsListAdapter);
    dialogsListAdapter.setItems(getDialogs());
  }

  private List<DefaultDialog> getDialogs() {
    return DialogsListFixtures.getChatList();
  }

  static class FixturesData {

    static final ArrayList<String> groupChatImages = new ArrayList<String>() {
      {
        add("http://i.imgur.com/hRShCT3.png");
        add("http://i.imgur.com/zgTUcL3.png");
        add("http://i.imgur.com/mRqh5w1.png");
      }
    };
    static final ArrayList<String> names = new ArrayList<String>() {
      {
        add("Samuel Reynolds");
        add("Kyle Hardman");
        add("Zoe Milton");
        add("Angel Ogden");
        add("Zoe Milton");
        add("Angelina Mackenzie");
        add("Kyle Oswald");
        add("Abigail Stevenson");
        add("Julia Goldman");
        add("Jordan Gill");
        add("Michelle Macey");
      }
    };
    static final ArrayList<String> groupChatTitles = new ArrayList<String>() {
      {
        add("Samuel, Michelle");
        add("Jordan, Jordan, Zoe");
        add("Julia, Angel, Kyle, Jordan");
      }
    };
    static final ArrayList<String> messages = new ArrayList<String>() {
      {
        add("Hello!");
        add("Hello! No problem. I can today at 2 pm. And after we can go to the office.");
        add("At first, for some time, I was not able to answer him one word");
        add("At length one of them called out in a clear, polite, smooth dialect, not unlike in "
            + "sound to the Italian");
        add("By the bye, Bob, said Hopkins");
        add("He made his passenger captain of one, with four of the men; and himself, his mate, "
            + "and five more, went in the other; and they contrived their business very well, for"
            + " they came up to the ship about midnight.");
        add("So saying he unbuckled his baldric with the bugle");
        add("Just then her head struck against the roof of the hall: in fact she was now more "
            + "than nine feet high, and she at once took up the little golden key and hurried off"
            + " to the garden door.");
      }
    };
    static SecureRandom rnd = new SecureRandom();
    static ArrayList<String> avatars = new ArrayList<String>() {
      {
        add("http://i.imgur.com/pv1tBmT.png");
        add("http://i.imgur.com/R3Jm1CL.png");
        add("http://i.imgur.com/ROz4Jgh.png");
        add("http://i.imgur.com/Qn9UesZ.png");
      }
    };

  }

  public static final class DialogsListFixtures extends FixturesData {
    private DialogsListFixtures() {
      throw new AssertionError();
    }

    static ArrayList<DefaultDialog> getChatList() {
      ArrayList<DefaultDialog> chats = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        chats.add(getDialog(i));
      }
      return chats;
    }

    private static IMessage getMessage(final Date date) {
      return new IMessage() {
        @Override
        public String getId() {
          return Long.toString(UUID.randomUUID().getLeastSignificantBits());
        }

        @Override
        public String getText() {
          return messages.get(rnd.nextInt(messages.size()));
        }

        @Override
        public IUser getUser() {
          return DialogsListFixtures.getUser();
        }

        @Override
        public Date getCreatedAt() {
          return date;
        }
      };
    }

    private static DefaultDialog getDialog(int i) {
      ArrayList<IUser> users = getUsers();
      return new DefaultDialog(String.valueOf(UUID.randomUUID().getLeastSignificantBits()),
          users.size() > 1 ? groupChatTitles.get(users.size() - 2) : users.get(0).getName(),
          users.size() > 1 ? groupChatImages.get(users.size() - 2) : avatars.get(rnd.nextInt(4)),
          users,
          getMessage(Calendar.getInstance().getTime()), i < 3 ? 3 - i : 0);
    }

    private static ArrayList<IUser> getUsers() {
      ArrayList<IUser> users = new ArrayList<>();
      int usersCount = 1 + rnd.nextInt(4);
      for (int i = 0; i < usersCount; i++) {
        users.add(getUser());
      }
      return users;
    }

    @NonNull
    private static IUser getUser() {
      return new DefaultUser(String.valueOf(UUID.randomUUID().getLeastSignificantBits()),
          names.get(rnd.nextInt(names.size())), avatars.get(rnd.nextInt(4)), rnd.nextBoolean());
    }
  }
}
