package com.yoloo.backend.question;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.Lists;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.vote.Vote;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuestionUtil {

    public static Map<Key<Question>, Question> aggregateCounts(Map<Key<Question>, Question> map,
                                                               QuestionShardService service) {
        Collection<Question> questions = map.values();

        final Map<Key<QuestionCounterShard>, QuestionCounterShard> shardMap =
                ofy().load().keys(service.createShardKeys(map.keySet()));

        for (Question question : questions) {
            long comments = 0L;
            long votes = 0L;

            int shardSize = question.getShardKeys().size();
            for (int i = 1; i <= shardSize; i++) {
                Key<QuestionCounterShard> shardKey = question.getShardKeys().get(i);

                if (shardMap.containsKey(shardKey)) {
                    QuestionCounterShard shard = shardMap.get(shardKey);
                    comments += shard.getComments();
                    votes += shard.getVotes();
                }
            }

            map.put(question.getKey(), question.withComments(comments).withVotes(votes));
        }

        return map;
    }

    public static Question aggregateCounts(Question question) {
        final Map<Key<QuestionCounterShard>, QuestionCounterShard> map =
                ofy().load().keys(question.getShardKeys());

        long comments = 0L;
        long votes = 0L;

        for (int i = 1; i <= QuestionCounterShard.SHARD_COUNT; i++) {
            Key<QuestionCounterShard> shardKey = question.getShardKeys().get(i - 1);

            if (map.containsKey(shardKey)) {
                QuestionCounterShard shard = map.get(shardKey);
                comments += shard.getComments();
                votes += shard.getVotes();
            }
        }

        return question.withComments(comments).withVotes(votes);
    }

    public static Map<Key<Question>, Question> aggregateVote(Key<Account> parentKey,
                                                             QuestionSorter sorter,
                                                             Map<Key<Question>, Question> map) {
        Pair<Question, Question> pair = getPostPairOrderedByDate(
                Lists.newArrayList(map.values()), sorter);

        QueryResultIterable<Vote> votes = ofy().load().type(Vote.class)
                .ancestor(parentKey)
                .filterKey(">=", Key.create(parentKey, Vote.class, pair.first.getWebsafeId()))
                .filterKey("<=", Key.create(parentKey, Vote.class, pair.second.getWebsafeId()))
                .iterable();

        for (Vote vote : votes) {
            //noinspection SuspiciousMethodCalls
            if (map.containsKey(vote.<Question>getVotableKey())) {
                //noinspection SuspiciousMethodCalls
                Question question = map
                        .get(vote.<Question>getVotableKey())
                        .withDir(vote.getDir());
                map.put(question.getKey(), question);
            }
        }

        return map;
    }

    /**
     * Uses an optimized linear time search
     * to find min and max Question which ordered by {@link Date}
     *
     * Normal linear search does (2n) comparison.
     * Optimized linear search does 3(n/2) comparison.
     *
     * @return Pair from Question (min, max)
     */
    public static Pair<Question, Question> getPostPairOrderedByDate(List<Question> posts,
                                                                    QuestionSorter sorter) {
        // Skip searching. List is already sorted by NEWEST.
        if (sorter.equals(QuestionSorter.NEWEST)) {
            return Pair.of(posts.get(0), posts.get(posts.size() - 1));
        } else {
            final int size = posts.size();
            final boolean odd = size % 2 == 1;
            final int till = odd ? size - 1 : size;

            Question lower = posts.get(0);
            Question upper = size == 1 ? lower : posts.get(1);

            Question bigger;
            Question smaller;

            for (int i = 2; i < till; i += 2) {
                if (posts.get(i).getCreated().isAfter(posts.get(i + 1).getCreated())) {
                    bigger = posts.get(i);
                    smaller = posts.get(i + 1);
                } else {
                    bigger = posts.get(i + 1);
                    smaller = posts.get(i);
                }

                lower = smaller.getCreated().isBefore(lower.getCreated())
                        ? smaller
                        : lower;
                upper = bigger.getCreated().isAfter(upper.getCreated())
                        ? bigger
                        : upper;
            }
            if (odd) {
                lower = posts.get(size - 1).getCreated().isBefore(lower.getCreated())
                        ? posts.get(size - 1)
                        : lower;

                upper = posts.get(size - 1).getCreated().isAfter(upper.getCreated())
                        ? posts.get(size - 1)
                        : upper;
            }

            return Pair.of(lower, upper);
        }
    }
}
