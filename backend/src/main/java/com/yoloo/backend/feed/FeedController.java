package com.yoloo.backend.feed;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.blog.Blog;
import com.yoloo.backend.question.Question;
import java.util.List;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class FeedController extends Controller {

  private static final Logger logger =
      Logger.getLogger(FeedController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  @NonNull
  private FeedService feedService;

  public CollectionResponse<FeedItem> list(Optional<Integer> limit, Optional<String> cursor,
      User user) {

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    Query<Feed> query = getFeedQuery(limit, cursor, accountKey);

    final QueryResultIterator<Feed> qi = query.iterator();

    List<FeedItem> feedItems = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      feedItems.add(qi.next().getFeedItem());
    }

    feedItems = feedService.mergeCounters(feedItems)
        .toList(DEFAULT_LIST_LIMIT)
        .flatMap(feedItems1 ->
            feedService.mergeVoteDirection(feedItems1, accountKey, false).toList())
        .blockingGet();

    return CollectionResponse.<FeedItem>builder()
        .setItems(feedItems)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  private Query<Feed> getFeedQuery(Optional<Integer> limit, Optional<String> cursor,
      Key<Account> accountKey) {
    Query<Feed> query = ofy()
        .load()
        .group(Question.ShardGroup.class, Blog.ShardGroup.class)
        .type(Feed.class)
        .ancestor(accountKey);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));
    return query;
  }
}
