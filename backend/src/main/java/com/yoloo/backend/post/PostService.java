package com.yoloo.backend.post;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;
import ix.Ix;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class PostService {

  private PostShardService postShardService;

  public PostEntity createPost(Account account, Optional<String> content, String tags,
      TravelerGroupEntity group, Optional<String> title, Optional<Integer> bounty,
      List<MediaEntity> medias, Tracker tracker, PostEntity.Type type) {

    final Key<PostEntity> postKey = factory().allocateId(account.getKey(), PostEntity.class);

    Map<Ref<PostEntity.PostShard>, PostEntity.PostShard> shardMap =
        postShardService.createShardMapWithRef(postKey);

    return PostEntity
        .builder()
        .id(postKey.getId())
        .parent(account.getKey())
        .ownerAvatarUrl(account.getAvatarUrl().getValue())
        .ownerUsername(account.getUsername())
        .title(title.orNull())
        .content(content.orNull())
        .shardRefs(ImmutableList.copyOf(shardMap.keySet()))
        .tags(ImmutableSet.copyOf(StringUtil.splitToIterable(tags, ",")))
        .travelerGroupKey(group.getKey())
        .dir(Vote.Direction.DEFAULT)
        .bounty(checkBounty(bounty, tracker))
        .acceptedCommentKey(null)
        .medias(Ix.from(medias).map(PostEntity.PostMedia::from).toList())
        .hasMedia(!medias.isEmpty())
        .commentCount(0L)
        .voteCount(0L)
        .commented(false)
        .postType(type.getType())
        .created(DateTime.now())
        .shardMap(shardMap)
        .owner(true)
        .build();
  }

  public List<Key<Comment>> getCommentKeys(Key<PostEntity> postKey) {
    return ofy()
        .load()
        .type(Comment.class)
        .filter(Comment.FIELD_POST_KEY + " =", postKey)
        .keys()
        .list();
  }

  public List<Key<Vote>> getVoteKeys(Key<PostEntity> postKey) {
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
