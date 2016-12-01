package com.yoloo.backend.comment;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.Lists;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.vote.Vote;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentUtil {

    public static Map<Key<Comment>, Comment> aggregateCounts(Map<Key<Comment>, Comment> map,
                                                             CommentShardService service) {
        Collection<Comment> comments = map.values();

        List<Key<CommentCounterShard>> shardKeys = service.getShardKeys(comments);

        final Map<Key<CommentCounterShard>, CommentCounterShard> shardMap =
                ofy().load().keys(shardKeys);

        for (Comment comment : comments) {
            long votes = 0L;

            for (int i = 1; i <= CommentCounterShard.SHARD_COUNT; i++) {
                Key<CommentCounterShard> shardKey = comment.getShardKeys().get(i - 1);

                if (shardMap.containsKey(shardKey)) {
                    CommentCounterShard shard = shardMap.get(shardKey);
                    votes += shard.getVotes();
                }
            }

            map.put(comment.getKey(), comment.withVotes(votes));
        }

        return map;
    }

    public static Comment aggregateCounts(Comment comment) {
        final Map<Key<CommentCounterShard>, CommentCounterShard> map =
                ofy().load().keys(comment.getShardKeys());

        long votes = 0L;

        for (int i = 1; i <= CommentCounterShard.SHARD_COUNT; i++) {
            CommentCounterShard shard = map.get(comment.getShardKeys().get(i - 1));
            votes += shard.getVotes();
        }

        return comment.withVotes(votes);
    }

    public static Map<Key<Comment>, Comment> aggregateVote(Key<Account> parentKey,
                                                           Map<Key<Comment>, Comment> map) {
        final Pair<Key<Vote>, Key<Vote>> votePair =
                sortAndGetVoteKeyPair(parentKey, Lists.newArrayList(map.values()));

        QueryResultIterable<Vote> votes = ofy().load().type(Vote.class)
                .ancestor(parentKey)
                .filterKey(">=", votePair.first)
                .filterKey("<=", votePair.second)
                .iterable();

        for (Vote vote : votes) {
            //noinspection SuspiciousMethodCalls
            if (map.containsKey(vote.<Comment>getVotableKey())) {
                //noinspection SuspiciousMethodCalls
                Comment comment = map
                        .get(vote.<Comment>getVotableKey())
                        .withDir(vote.getDir());
                map.put(comment.getKey(), comment);
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public static Comment aggregateVote(Key<Account> parentKey, Comment comment) {
        Pair<Key<Vote>, Key<Vote>> votePair =
                sortAndGetVoteKeyPair(parentKey, Collections.singletonList(comment));

        QueryResultIterable<Vote> votes = ofy().load().type(Vote.class)
                .ancestor(parentKey)
                .filterKey(">=", votePair.first)
                .filterKey("<=", votePair.second)
                .iterable();

        for (Vote vote : votes) {
            if (comment.getKey().equivalent((Key<Comment>) vote.<Comment>getVotableKey())) {
                comment = comment.withDir(vote.getDir());
            }
        }

        return comment;
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
    public static Pair<Key<Vote>, Key<Vote>> sortAndGetVoteKeyPair(Key<Account> parentKey,
                                                                   List<Comment> comments) {
        final int size = comments.size();
        final boolean odd = size % 2 == 1;
        final int till = odd ? size - 1 : size;

        Comment lower = comments.get(0);
        Comment upper = size == 1 ? lower : comments.get(1);

        Comment bigger;
        Comment smaller;

        for (int i = 2; i < till; i += 2) {
            if (comments.get(i).getCreated().isAfter(comments.get(i + 1).getCreated())) {
                bigger = comments.get(i);
                smaller = comments.get(i + 1);
            } else {
                bigger = comments.get(i + 1);
                smaller = comments.get(i);
            }

            lower = smaller.getCreated().isBefore(lower.getCreated())
                    ? smaller
                    : lower;
            upper = bigger.getCreated().isAfter(upper.getCreated())
                    ? bigger
                    : upper;
        }
        if (odd) {
            lower = comments.get(size - 1).getCreated().isBefore(lower.getCreated())
                    ? comments.get(size - 1)
                    : lower;

            upper = comments.get(size - 1).getCreated().isAfter(upper.getCreated())
                    ? comments.get(size - 1)
                    : upper;
        }

        Key<Vote> lowerVoteKey = Key.create(parentKey, Vote.class, lower.getWebsafeId());
        Key<Vote> upperVoteKey = Key.create(parentKey, Vote.class, upper.getWebsafeId());

        return Pair.of(lowerVoteKey, upperVoteKey);
    }
}
