package com.yoloo.backend.post;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;
import io.reactivex.Observable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class PostService {

  private PostShardService postShardService;

  public Post createPost(Account account, String content, String tags, TravelerGroupEntity group,
      Optional<String> title, Optional<Integer> bounty, List<Media> medias, Tracker tracker,
      Post.PostType postType) {

    final Key<Post> postKey = factory().allocateId(account.getKey(), Post.class);

    Map<Ref<PostShard>, PostShard> shardMap = postShardService.createShardMapWithRef(postKey);

    return Post
        .builder()
        .id(postKey.getId())
        .parent(account.getKey())
        .avatarUrl(account.getAvatarUrl())
        .username(account.getUsername())
        .title(title.orNull())
        .content(content)
        .shardRefs(ImmutableList.copyOf(shardMap.keySet()))
        .tags(ImmutableSet.copyOf(StringUtil.splitToIterable(tags, ",")))
        .travelerGroup(group.getKey())
        .dir(Vote.Direction.DEFAULT)
        .bounty(checkBounty(bounty, tracker))
        .acceptedCommentKey(null)
        .medias(medias)
        .commentCount(0L)
        .voteCount(0L)
        .reportCount(0)
        .commented(null)
        .postType(postType)
        .created(DateTime.now())
        .shardMap(shardMap)
        .build();
  }

  public Observable<QueryResultIterable<Key<Comment>>> getCommentKeysObservable(Key<Post> postKey) {
    return Observable.fromCallable(() -> ofy()
        .load()
        .type(Comment.class)
        .filter(Comment.FIELD_QUESTION_KEY + " =", postKey)
        .keys()
        .iterable());
  }

  public Observable<QueryResultIterable<Key<Vote>>> getVoteKeysObservable(Key<Post> postKey) {
    return Observable.fromCallable(() -> ofy()
        .load()
        .type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", postKey)
        .keys()
        .iterable());
  }

  public List<Key<Comment>> getCommentKeys(Key<Post> postKey) {
    return ofy()
        .load()
        .type(Comment.class)
        .filter(Comment.FIELD_QUESTION_KEY + " =", postKey)
        .keys()
        .list();
  }

  public List<Key<Vote>> getVoteKeys(Key<Post> postKey) {
    return ofy()
        .load()
        .type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", postKey)
        .keys()
        .list();
  }

  private int checkBounty(Optional<Integer> bounty, Tracker tracker) {
    return bounty.isPresent()
        ? (tracker.hasEnoughBounty(bounty.get()) ? bounty.get() : 0)
        : bounty.or(0);
  }
}
