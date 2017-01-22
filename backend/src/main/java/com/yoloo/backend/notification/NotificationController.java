package com.yoloo.backend.notification;

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
import java.util.List;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public class NotificationController extends Controller {

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  public CollectionResponse<Notification> list(Optional<String> cursor, Optional<Integer> limit,
      User user) {
    final Key<Account> authKey = Key.create(user.getUserId());

    Query<Notification> query = ofy().load().type(Notification.class)
        .ancestor(authKey)
        .order("-" + Notification.FIELD_CREATED);

    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Notification> qi = query.iterator();

    List<Notification> notifications = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      notifications.add(qi.next());
    }

    return CollectionResponse.<Notification>builder()
        .setItems(notifications)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }
}
