package com.yoloo.backend.media;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Iterator;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class MediaController extends Controller {

  private static final Logger logger =
      Logger.getLogger(MediaController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  @NonNull
  private MediaService mediaService;

  /**
   * List collection response.
   *
   * @param webssafeAccountId the webssafe account id
   * @param limit the limit
   * @param cursor the cursor
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Media> list(String webssafeAccountId, Optional<Integer> limit,
      Optional<String> cursor, User user) {

    // Create account key from websafe id.
    final Key<Account> authKey = Key.create(webssafeAccountId);

    Query<Media> query = ofy().load().type(Media.class).ancestor(authKey);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Media> qi = query.iterator();

    return Observable.just(qi)
        .map(Iterator::next)
        .toList(DEFAULT_LIST_LIMIT)
        .compose(upstream -> {
          CollectionResponse<Media> response = CollectionResponse.<Media>builder()
              .setItems(upstream.blockingGet())
              .setNextPageToken(qi.getCursor().toWebSafeString())
              .build();

          return Single.just(response);
        })
        .blockingGet();
  }
}
