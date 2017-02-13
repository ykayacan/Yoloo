package com.yoloo.backend.vote;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteUtil {

  /**
   * Uses an optimized linear time search to find min and max Question which ordered by {@link
   * org.joda.time.DateTime}
   *
   * Normal linear search does (2n) comparison. Optimized linear search does 3(n/2) comparison.
   *
   * @param <T> the type parameter
   * @param entities the votables
   * @param sorted the is pre ordered
   * @return the pair
   */
  public static <T extends Votable> Pair<T, T> findFirstAndLastElement3(Collection<T> entities,
      boolean sorted) {
    ImmutableList<T> elements = ImmutableList.copyOf(entities);
    final int size = elements.size();

    if (sorted) {
      return Pair.of(elements.get(size - 1), elements.get(0));
    } else {
      T min = elements.get(0);
      T max = size == 1 ? min : elements.get(1);

      T bigger;
      T smaller;

      final boolean odd = size % 2 == 1;
      final int till = odd ? size - 1 : size;

      for (int i = 2; i < till; i += 2) {
        T before = elements.get(i);
        T next = elements.get(i + 1);

        if (before.getCreated().isAfter(next.getCreated())
            || before.getCreated().isEqual(next.getCreated())) {
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
        T before = elements.get(size - 1);

        min = before.getCreated().isBefore(min.getCreated()) ? before : min;
        max = before.getCreated().isAfter(max.getCreated()) ? before : max;
      }

      return Pair.of(min, max);
    }
  }

  private static <T extends Votable> Pair<T, T> findFirstAndLastElement(List<T> entities,
      boolean sorted) {
    final int size = entities.size();

    if (sorted) {
      return Pair.of(entities.get(size - 1), entities.get(0));
    } else {
      T min = entities.get(0);
      T max = size == 1 ? min : entities.get(1);

      for (int i = 0; i < size; i++) {
        if (entities.get(i).getCreated().isBefore(min.getCreated())) {
          min = entities.get(i);
        }
        if (entities.get(i).getCreated().isAfter(max.getCreated())) {
          max = entities.get(i);
        }
      }

      return Pair.of(min, max);
    }
  }

  public static <T extends Votable> QueryResultIterable<Vote> mergeVotes(List<T> entities,
      Key<Account> accountKey, boolean sorted) {
    Pair<T, T> elementPair = findFirstAndLastElement(entities, sorted);

    return ofy().load().type(Vote.class).ancestor(accountKey)
        .filterKey(">=", Vote.createKey(elementPair.first.getVotableKey(), accountKey))
        .filterKey("<=", Vote.createKey(elementPair.second.getVotableKey(), accountKey))
        .iterable();
  }
}
