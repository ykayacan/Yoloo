package com.yoloo.backend.comment;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.Lists;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.vote.Vote;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentUtil {

    public static Map<Key<Comment>, Comment> aggregateCounts(Map<Key<Comment>, Comment> commentMap,
                                                             CommentShardService service) {
        List<Key<CommentCounterShard>> shardKeys = service.getShardKeys(commentMap.keySet());
        final Map<Key<CommentCounterShard>, CommentCounterShard> shardMap =
                ofy().load().keys(shardKeys);

        for (Comment comment : commentMap.values()) {
            long votes = 0L;

            for (int i = 1; i <= CommentCounterShard.SHARD_COUNT; i++) {
                Key<CommentCounterShard> shardKey =
                        Key.create(CommentCounterShard.class, createShardId(comment.getKey(), i));

                if (shardMap.containsKey(shardKey)) {
                    CommentCounterShard shard = shardMap.get(shardKey);
                    votes += shard.getVotes();
                }
            }

            commentMap.put(comment.getKey(), comment.withVotes(votes));
        }

        return commentMap;
    }

    public static Comment aggregateCounts(Comment comment, CommentShardService service) {
        List<Key<CommentCounterShard>> shardKeys = service.getShardKeys(comment.getKey());
        final Map<Key<CommentCounterShard>, CommentCounterShard> shardMap =
                ofy().load().keys(shardKeys);

        long votes = 0L;

        for (int i = 1; i <= CommentCounterShard.SHARD_COUNT; i++) {
            Key<CommentCounterShard> shardKey =
                    Key.create(CommentCounterShard.class, createShardId(comment.getKey(), i));
            CommentCounterShard shard = shardMap.get(shardKey);
            votes += shard.getVotes();
        }

        return comment.withVotes(votes);
    }

    public static Map<Key<Comment>, Comment> aggregateVote(Key<Account> parentKey,
                                                           Map<Key<Comment>, Comment> commentMap) {
        Pair<Comment, Comment> pair = getCommentPairOrderedByDate(
                Lists.newArrayList(commentMap.values()));

        QueryResultIterable<Vote> votes = ofy().load().type(Vote.class)
                .ancestor(parentKey)
                .filterKey(">=", Key.create(parentKey, Vote.class, pair.first.getWebsafeId()))
                .filterKey("<=", Key.create(parentKey, Vote.class, pair.second.getWebsafeId()))
                .iterable();

        for (Vote vote : votes) {
            //noinspection SuspiciousMethodCalls
            if (commentMap.containsKey(vote.<Comment>getVotableKey())) {
                //noinspection SuspiciousMethodCalls
                Comment comment = commentMap
                        .get(vote.<Comment>getVotableKey())
                        .withDir(vote.getDir());
                commentMap.put(comment.getKey(), comment);
            }
        }

        return commentMap;
    }

    @SuppressWarnings("unchecked")
    public static Comment aggregateVote(Key<Account> parentKey, Comment comment) {
        Pair<Comment, Comment> pair = getCommentPairOrderedByDate(Lists.newArrayList(comment));

        QueryResultIterable<Vote> votes = ofy().load().type(Vote.class)
                .ancestor(parentKey)
                .filterKey(">=", Key.create(parentKey, Vote.class, pair.first.getWebsafeId()))
                .filterKey("<=", Key.create(parentKey, Vote.class, pair.second.getWebsafeId()))
                .iterable();

        for (Vote vote : votes) {
            if (comment.getKey().equivalent((Key<Comment>) vote.<Comment>getVotableKey())) {
                comment = comment.withDir(vote.getDir());
            }
        }

        return comment;
    }

    public static String createShardId(Key<Comment> commentKey, int shardNum) {
        return commentKey.toWebSafeString() + ":" + String.valueOf(shardNum);
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
    public static Pair<Comment, Comment> getCommentPairOrderedByDate(List<Comment> comments) {
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

        return Pair.of(lower, upper);
    }
}
