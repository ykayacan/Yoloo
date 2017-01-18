package com.yoloo.backend.feed;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.blog.Blog;
import com.yoloo.backend.blog.BlogCounterShard;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class FeedService {

  public Observable<FeedItem> mergeCounters(Collection<FeedItem> items) {
    if (items.isEmpty()) return Observable.empty();

    return Observable.fromIterable(items).flatMap(this::mergeCounters);
  }

  private Observable<FeedItem> mergeCounters(FeedItem item) {
    return Observable.fromIterable(item.getShards())
        .cast(FeedShard.class)
        .reduce(this::reduceShards)
        .map(feedShard -> mapToFeedItem(item, feedShard))
        .toObservable();
  }

  public Observable<FeedItem> mergeVoteDirection(List<FeedItem> items,
      Key<Account> parentKey, boolean sorted) {
    if (items.isEmpty()) return Observable.empty();

    final VoteHelper helper = VoteHelper.create();

    Pair<FeedItem, FeedItem> pair = helper.sort(items, sorted);
    QueryResultIterable<Vote> votes = helper.getVotes(pair.first, pair.second, parentKey);

    return Observable
        .fromIterable(items)
        .flatMap(item -> checkVoteMatch(votes, item));
  }

  private FeedShard reduceShards(FeedShard s1, FeedShard s2) throws Exception {
    if (s1 instanceof QuestionCounterShard && s2 instanceof QuestionCounterShard) {
      QuestionCounterShard qcs1 = (QuestionCounterShard) s1;
      QuestionCounterShard qcs2 = (QuestionCounterShard) s2;
      return qcs1.addValues(qcs2.getComments(), qcs2.getVotes(), qcs2.getReports());
    } else if (s1 instanceof BlogCounterShard && s2 instanceof BlogCounterShard) {
      BlogCounterShard bcs1 = (BlogCounterShard) s1;
      BlogCounterShard bcs2 = (BlogCounterShard) s2;
      return bcs1.addValues(bcs2.getComments(), bcs2.getVotes(), bcs2.getReports());
    } else {
      throw new Exception("Unsupported operation. Shard must extend FeedShard!");
    }
  }

  private FeedItem mapToFeedItem(FeedItem item, FeedShard shard) throws Exception {
    if (item instanceof Question && shard instanceof QuestionCounterShard) {
      Question question = (Question) item;
      QuestionCounterShard qcs = (QuestionCounterShard) shard;

      return question
          .withComments(qcs.getComments())
          .withVotes(qcs.getVotes())
          .withReports(qcs.getReports());
    } else if (item instanceof Blog && shard instanceof BlogCounterShard) {
      Blog blog = (Blog) item;
      BlogCounterShard qcs = (BlogCounterShard) shard;

      return blog
          .withComments(qcs.getComments())
          .withVotes(qcs.getVotes())
          .withReports(qcs.getReports());
    } else {
      throw new Exception("Unsupported operation.");
    }
  }

  private ObservableSource<? extends FeedItem> checkVoteMatch(
      QueryResultIterable<Vote> votes, FeedItem item) {
    for (Vote vote : votes) {
      if (item instanceof Question) {
        Question question = (Question) item;

        if (question.getKey().equals(vote.getVotableKey())) {
          item = question.withDir(vote.getDir());
        }
      } else if (item instanceof Blog) {
        Blog blog = (Blog) item;

        if (blog.getKey().equals(vote.getVotableKey())) {
          item = blog.withDir(vote.getDir());
        }
      }
    }

    return Observable.just(item);
  }
}
