package com.yoloo.backend.post.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.relationship.Relationship;
import io.reactivex.Observable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
public class UpdateFeedTask extends HttpServlet {

  private static final String QUEUE_NAME = "update-feed-queue";
  private static final String URL = "/tasks/update/feed";

  private static final String USER_ID = "userId";
  private static final String POST_ID = "postId";
  private static final String GROUP_ID = "groupId";

  public static void addToQueue(@Nonnull String userId, @Nonnull String postId,
      @Nonnull String groupId) {
    Queue queue = QueueFactory.getQueue(QUEUE_NAME);
    queue.addAsync(TaskOptions.Builder.withUrl(URL)
        .param(USER_ID, userId)
        .param(POST_ID, postId)
        .param(GROUP_ID, groupId));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(USER_ID);
    final String postId = req.getParameter(POST_ID);
    final String groupId = req.getParameter(GROUP_ID);

    final Key<Account> accountKey = Key.create(accountId);
    final Key<PostEntity> postKey = Key.create(postId);
    final Key<TravelerGroupEntity> groupKey = Key.create(groupId);

    Observable
        .merge(findFollowersOfUserObservable(accountKey),
            findSubscribersOfGroupObservable(groupKey))
        .distinct()
        .map(key -> Feed.builder().id(Feed.createId(postKey)).parent(key).build())
        .toList()
        .subscribe(feeds -> ofy().save().entities(feeds),
            throwable -> log("An error occurred while processing feed", throwable));
  }

  private Observable<Key<Account>> findFollowersOfUserObservable(Key<Account> accountKey) {
    return Observable
        .fromCallable(() -> ofy().load()
            .type(Relationship.class)
            .filter(Relationship.FIELD_FOLLOWING_KEY, accountKey)
            .keys()
            .iterable())
        .flatMap(Observable::fromIterable)
        .map(Key::<Account>getParent)
        .concatWith(Observable.just(accountKey));
  }

  private Observable<Key<Account>> findSubscribersOfGroupObservable(
      Key<TravelerGroupEntity> groupKey) {
    return Observable
        .fromCallable(() -> ofy().load()
            .type(Account.class)
            .filter(Account.FIELD_SUBSCRIBED_GROUP_KEYS, groupKey)
            .keys()
            .iterable())
        .flatMap(Observable::fromIterable);
  }
}
