package com.yoloo.android.feature.chat.compose;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class ComposePresenter extends MvpPresenter<ComposeView> {

  private final UserRepository userRepository;
  private final DatabaseReference rooRef;

  public ComposePresenter(UserRepository userRepository, DatabaseReference rooRef) {
    this.userRepository = userRepository;
    this.rooRef = rooRef;
  }

  public void loadFollowers(String userId) {
    /*Disposable d = userRepository.listFollowers(userId, null, null, 40)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> getView().onLoaded(response),
            throwable -> getView().onError(throwable));

    getDisposable().addPost(d);*/

    List<AccountRealm> list = new ArrayList<>();
    list.add(AccountFaker.generateOne());

    Response<List<AccountRealm>> response = Response.create(list, null, null);

    getView().onLoaded(response);
  }

  public void createConversation(String ownerId, AccountRealm target) {
    /*String key = rooRef.child("members").push().getKey();

    Map<String, Boolean> indices = new HashMap<>();
    indices.put(ownerId, true);
    indices.put(target.getId(), true);

    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("members/" + key, indices);

    Chat conversation =
        new Chat(target.getUsername(), target.getAvatarUrl(), "Test");

    childUpdates.put("conversations/" + key, conversation);

    rooRef.updateChildren(childUpdates);*/

    rooRef.child("members").orderByChild(ownerId).equalTo(true)
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
              String key = snapshot.getKey();
              //Timber.d("Key: %s", key);
              rooRef.child("conversations").child(key)
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                      Chat c = dataSnapshot.getValue(Chat.class);

                      Timber.d("Chat: %s", c.toString());
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                  });
            }
          }

          @Override public void onCancelled(DatabaseError databaseError) {

          }
        });
  }
}
