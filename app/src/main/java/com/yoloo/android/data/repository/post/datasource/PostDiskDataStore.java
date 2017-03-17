package com.yoloo.android.data.repository.post.datasource;

import com.annimon.stream.Optional;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class PostDiskDataStore {

  private static PostDiskDataStore instance;

  private PostDiskDataStore() {
  }

  public static PostDiskDataStore getInstance() {
    if (instance == null) {
      instance = new PostDiskDataStore();
    }
    return instance;
  }

  public Single<Optional<PostRealm>> get(@Nonnull String postId) {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm result = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, postId)
          .findFirst();

      Optional<PostRealm> post = result == null
          ? Optional.empty()
          : Optional.of(realm.copyFromRealm(result));

      realm.close();

      return post;
    });
  }

  public void add(@Nonnull PostRealm post) {
    addAll(Collections.singletonList(post));
  }

  public void addAll(@Nonnull List<PostRealm> posts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(posts));
    realm.close();
  }

  public Completable delete(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class)
            .equalTo(PostRealmFields.ID, postId)
            .findFirst();

        if (post != null) {
          post.deleteFromRealm();
        }

        RealmResults<CommentRealm> comments = tx.where(CommentRealm.class)
            .equalTo(CommentRealmFields.POST_ID, postId)
            .findAll();

        if (comments != null && !comments.isEmpty()) {
          comments.deleteAllFromRealm();
        }
      });

      realm.close();
    });
  }

  public Observable<Response<List<PostRealm>>> listByFeed() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.IS_FEED_ITEM, true)
          .notEqualTo(PostRealmFields.PENDING, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);

      List<PostRealm> posts = results.isEmpty()
          ? Collections.emptyList()
          : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  public Observable<Response<List<PostRealm>>> listByBounty() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm.where(PostRealm.class)
          .notEqualTo(PostRealmFields.PENDING, true)
          .notEqualTo(PostRealmFields.BOUNTY, 0)
          .findAllSorted(PostRealmFields.BOUNTY, Sort.DESCENDING);

      List<PostRealm> posts = results.isEmpty()
          ? Collections.emptyList()
          : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  public Observable<Response<List<PostRealm>>> listBookmarkedPosts() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.BOOKMARKED, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);

      List<PostRealm> posts = results.isEmpty()
          ? Collections.emptyList()
          : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  public Completable vote(@Nonnull String postId, int direction) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class)
            .equalTo(PostRealmFields.ID, postId)
            .findFirst();

        if (post != null) {
          setVoteCounter(direction, post);
          post.setVoteDir(direction);
          tx.insertOrUpdate(post);
        }
      });

      realm.close();
    });
  }

  public Completable bookmark(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, postId)
          .findFirst();

      realm.executeTransaction(tx -> {
        post.setBookmarked(true);
        tx.insertOrUpdate(post);
      });

      realm.close();
    });
  }

  public Completable unBookmark(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, postId)
          .findFirst();

      realm.executeTransaction(tx -> {
        post.setBookmarked(false);
        tx.insertOrUpdate(post);
      });

      realm.close();
    });
  }

  private void setVoteCounter(int direction, PostRealm post) {
    switch (post.getVoteDir()) {
      case 0:
        if (direction == 1) {
          post.increaseVoteCount();
        } else if (direction == -1) {
          post.decreaseVoteCount();
        }
        break;
      case 1:
        if (direction == 0) {
          post.decreaseVoteCount();
        } else if (direction == -1) {
          post.decreaseVoteCount();
          post.decreaseVoteCount();
        }
        break;
      case -1:
        if (direction == 0) {
          post.increaseVoteCount();
        } else if (direction == 1) {
          post.increaseVoteCount();
          post.increaseVoteCount();
        }
        break;
    }
  }
}
