package com.yoloo.android.data.repository.post;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.CommentRealmFields;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.db.PostRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

class PostDiskDataStore {

  private static PostDiskDataStore instance;

  private PostDiskDataStore() {
    // empty constructor
  }

  static PostDiskDataStore getInstance() {
    if (instance == null) {
      instance = new PostDiskDataStore();
    }
    return instance;
  }

  Single<Optional<PostRealm>> get(@Nonnull String postId) {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm result =
          realm.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

      Optional<PostRealm> post =
          result == null ? Optional.empty() : Optional.of(realm.copyFromRealm(result));

      realm.close();

      return post;
    });
  }

  void add(@Nonnull PostRealm post) {
    addAll(Collections.singletonList(post));
  }

  void addAll(@Nonnull List<PostRealm> posts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(posts));
    realm.close();
  }

  void addTrendingBlogs(@Nonnull List<PostRealm> posts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(
        Stream.of(posts).map(post -> post.setFeedItem(false).setPending(false)).toList()));
    realm.close();
  }

  Completable delete(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

        if (post != null) {
          post.deleteFromRealm();
        }

        RealmResults<CommentRealm> comments =
            tx.where(CommentRealm.class).equalTo(CommentRealmFields.POST_ID, postId).findAll();

        if (comments != null && !comments.isEmpty()) {
          comments.deleteAllFromRealm();
        }
      });

      realm.close();
    });
  }

  Observable<Response<List<PostRealm>>> listByFeed() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm
          .where(PostRealm.class)
          .equalTo(PostRealmFields.FEED_ITEM, true)
          .notEqualTo(PostRealmFields.PENDING, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);

      List<PostRealm> posts =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  Observable<Response<List<PostRealm>>> listByBounty() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm
          .where(PostRealm.class)
          .equalTo(PostRealmFields.PENDING, false)
          .notEqualTo(PostRealmFields.BOUNTY, 0)
          .findAllSorted(PostRealmFields.BOUNTY, Sort.DESCENDING);

      List<PostRealm> posts =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  Observable<Response<List<PostRealm>>> listByBookmarkedPosts() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm
          .where(PostRealm.class)
          .equalTo(PostRealmFields.BOOKMARKED, true)
          .findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING);

      List<PostRealm> posts =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results);

      realm.close();

      return Response.create(posts, null);
    });
  }

  Observable<Response<List<PostRealm>>> listByTrendingBlogPosts(int limit) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm
          .where(PostRealm.class)
          .equalTo(PostRealmFields.FEED_ITEM, false)
          .equalTo(PostRealmFields.PENDING, false)
          .equalTo(PostRealmFields.POST_TYPE, PostRealm.TYPE_BLOG)
          .findAllSorted(PostRealmFields.RANK, Sort.DESCENDING);

      List<PostRealm> posts =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results, limit);

      realm.close();

      return Response.create(posts, null);
    });
  }

  Completable vote(@Nonnull String postId, int direction) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

        if (post != null) {
          setVoteCounter(direction, post);
          post.setVoteDir(direction);
          tx.insertOrUpdate(post);
        }
      });

      realm.close();
    });
  }

  Completable bookmark(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

      if (post != null) {
        realm.executeTransaction(tx -> {
          post.setBookmarked(true);
          tx.insertOrUpdate(post);
        });
      }

      realm.close();
    });
  }

  Completable unBookmark(@Nonnull String postId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      PostRealm post = realm.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

      if (post != null) {
        realm.executeTransaction(tx -> {
          post.setBookmarked(false);
          tx.insertOrUpdate(post);
        });
      }

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
