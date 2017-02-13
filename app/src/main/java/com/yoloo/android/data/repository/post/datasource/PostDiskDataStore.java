package com.yoloo.android.data.repository.post.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

public class PostDiskDataStore {

  private static PostDiskDataStore INSTANCE;

  private PostDiskDataStore() {
  }

  public static PostDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PostDiskDataStore();
    }
    return INSTANCE;
  }

  public Observable<PostRealm> get(String postId) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.copyFromRealm(realm.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, postId)
          .findFirst());

      realm.close();

      return post;
    });
  }

  public void add(PostRealm post) {
    addAll(Collections.singletonList(post));
  }

  public void addAll(List<PostRealm> posts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(posts));
    realm.close();
  }

  public Completable delete(String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        tx.where(PostRealm.class)
            .equalTo(PostRealmFields.ID, postId)
            .findFirst().deleteFromRealm();

        tx.where(CommentRealm.class)
            .equalTo(CommentRealmFields.POST_ID, postId)
            .findAll().deleteAllFromRealm();
      });

      realm.close();
    });
  }

  public Observable<Response<List<PostRealm>>> listByFeed() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<PostRealm> posts = realm.copyFromRealm(realm.where(PostRealm.class)
          .equalTo(PostRealmFields.IS_FEED_ITEM, true)
          .notEqualTo(PostRealmFields.PENDING, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING));

      realm.close();

      return Response.create(posts, null, null);
    });
  }

  public Observable<Response<List<PostRealm>>> listByCategory(String categoryName,
      PostSorter sorter) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmQuery<PostRealm> query = realm.where(PostRealm.class);

      RealmResults<PostRealm> results;

      query.notEqualTo(PostRealmFields.PENDING, true);

      if (sorter.equals(PostSorter.NEWEST)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, categoryName)
            .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.HOT)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, categoryName)
            .findAllSorted(PostRealmFields.RANK, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.UNANSWERED)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, categoryName)
            .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.BOUNTY)) {
        results = query.notEqualTo(PostRealmFields.BOUNTY, 0)
            .findAllSorted(PostRealmFields.RANK, Sort.DESCENDING);
      } else {
        results = query.findAll();
      }

      List<PostRealm> posts = realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null, null);
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

      return Response.create(posts, null, null);
    });
  }

  public Observable<Response<List<PostRealm>>> listBookmarkedPosts() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<PostRealm> posts = realm.copyFromRealm(realm.where(PostRealm.class)
          .equalTo(PostRealmFields.BOOKMARKED, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING));

      realm.close();

      return Response.create(posts, null, null);
    });
  }

  public Completable vote(String postId, int direction) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, postId)
          .findFirst();

      realm.executeTransaction(tx -> {
        setVoteCounter(direction, post);
        post.setDir(direction);
        tx.insertOrUpdate(post);
      });

      realm.close();
    });
  }

  public Completable bookmark(String postId) {
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

  public Completable unBookmark(String postId) {
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
    switch (post.getDir()) {
      case 0:
        if (direction == 1) {
          post.increaseVotes();
        } else if (direction == -1) {
          post.decreaseVotes();
        }
        break;
      case 1:
        if (direction == 0) {
          post.decreaseVotes();
        } else if (direction == -1) {
          post.decreaseVotes();
          post.decreaseVotes();
        }
        break;
      case -1:
        if (direction == 0) {
          post.increaseVotes();
        } else if (direction == 1) {
          post.increaseVotes();
          post.increaseVotes();
        }
        break;
    }
  }
}