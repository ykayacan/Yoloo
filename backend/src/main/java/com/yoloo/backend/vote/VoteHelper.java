package com.yoloo.backend.vote;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public final class VoteHelper {

  /**
   * Gets votes.
   *
   * @param <T> the type parameter
   * @param lower the lower
   * @param upper the upper
   * @param parentKey the parent key
   * @return the votes
   */
  public <T extends Votable> QueryResultIterable<Vote> getVotes(T lower, T upper,
      Key<Account> parentKey) {
    return ofy().load().type(Vote.class).ancestor(parentKey)
        .filterKey(">=", Vote.createKey(lower.<Question>getVotableKey(), parentKey))
        .filterKey("<=", Vote.createKey(upper.<Question>getVotableKey(), parentKey))
        .iterable();
  }

  /**
   * Uses an optimized linear time search to find min and max Question which ordered by {@link
   * org.joda.time.DateTime}*
   *
   * Normal linear search does (2n) comparison. Optimized linear search does 3(n/2) comparison.
   *
   * @param <T> the type parameter
   * @param entities the votables
   * @param sorted the is pre ordered
   * @return the pair
   */
  public <T extends Votable> Pair<T, T> sort(Collection<T> entities, boolean sorted) {
    List<T> list = Lists.newArrayList(entities);
    final int size = list.size();

    if (sorted) {
      return Pair.of(list.get(size - 1), list.get(0));
    } else {
      T min = list.get(0);
      T max = size == 1 ? min : list.get(1);

      T bigger;
      T smaller;

      final boolean odd = size % 2 == 1;
      final int till = odd ? size - 1 : size;

      for (int i = 2; i < till; i += 2) {
        T before = list.get(i);
        T next = list.get(i + 1);

        if (before.getCreated().isAfter(next.getCreated()) ||
            before.getCreated().isEqual(next.getCreated())) {
          bigger = before;
          smaller = next;
        } else {
          bigger = next;
          smaller = before;
        }

        min = smaller.getCreated().isBefore(min.getCreated()) ? smaller : min;
        max = bigger.getCreated().isAfter(max.getCreated()) ? bigger : max;
      }
      if (odd) {
        T before = list.get(size - 1);

        min = before.getCreated().isBefore(min.getCreated()) ? before : min;
        max = before.getCreated().isAfter(max.getCreated()) ? before : max;
      }

      return Pair.of(min, max);
    }
  }
}