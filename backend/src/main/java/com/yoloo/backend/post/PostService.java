package com.yoloo.backend.post;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class PostService {

  private PostShardService postShardService;

  public PostEntity create(
      Account account,
      String content,
      String tags,
      String categories,
      Optional<String> title,
      Optional<Integer> bounty,
      Media media,
      Tracker tracker,
      Post.PostType postType) {

    final Key<Post> postKey = factory().allocateId(account.getKey(), Post.class);

    Map<Ref<PostShard>, PostShard> shardMap = postShardService.createShardMapWithRef(postKey);

    Post post = Post.builder()
        .id(postKey.getId())
        .parent(account.getKey())
        .avatarUrl(account.getAvatarUrl())
        .username(account.getUsername())
        .title(title.orNull())
        .content(content)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .tags(StringUtil.split(tags, ","))
        .categories(StringUtil.split(categories, ","))
        .dir(Vote.Direction.DEFAULT)
        .bounty(checkBounty(bounty, tracker))
        .acceptedCommentKey(null)
        .media(media)
        .commentCount(0L)
        .voteCount(0L)
        .reportCount(0)
        .commented(false)
        .postType(postType)
        .created(DateTime.now())
        .build();

    return PostEntity.builder().post(post).shards(shardMap).build();
  }

  public List<Key<Comment>> getCommentKeys(Key<Post> questionKey) {
    return ofy().load().type(Comment.class)
        .filter(Comment.FIELD_QUESTION_KEY + " =", questionKey)
        .keys().list();
  }

  public List<Key<Vote>> getVoteKeys(Key<Post> questionKey) {
    return ofy().load().type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", questionKey)
        .keys().list();
  }

  private int checkBounty(Optional<Integer> bounty, Tracker tracker) {
    if (bounty.isPresent()) {
      return tracker.getBounties() >= bounty.get() ? bounty.get() : 0;
    } else {
      return bounty.or(0);
    }
  }
}
