package com.yoloo.backend.blog;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;
import io.reactivex.Observable;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;

@AllArgsConstructor(staticName = "create")
public class BlogService {

  public BlogEntity create(Account account, String title, String content, String tags,
      String categories, Media media, Tracker tracker) {

    final Key<Blog> blogKey = factory().allocateId(account.getKey(), Blog.class);

    return Observable.range(1, QuestionCounterShard.SHARD_COUNT)
        .map(shardNum -> buildShard(blogKey, shardNum))
        .toMap(Ref::create)
        .map(map -> buildBlogEntity(blogKey, account, title, content, tags, categories, media,
            tracker, map))
        .blockingGet();
  }

  private BlogCounterShard buildShard(Key<Blog> blogKey, Integer shardNum) {
    return BlogCounterShard.builder()
        .id(ShardUtil.generateShardId(blogKey, shardNum))
        .comments(0L)
        .votes(0L)
        .reports(0)
        .build();
  }

  private BlogEntity buildBlogEntity(Key<Blog> blogKey, Account account, String title,
      String content, String tags, String categories, Media media, Tracker tracker,
      Map<Ref<BlogCounterShard>, BlogCounterShard> shardMap) {

    Blog blog = Blog.builder()
        .id(blogKey.getId())
        .parent(account.getKey())
        .avatarUrl(account.getAvatarUrl())
        .username(account.getUsername())
        .title(title)
        .content(content)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .tags(StringUtil.splitToSet(tags, ","))
        .categories(StringUtil.splitToSet(categories, ","))
        .dir(Vote.Direction.DEFAULT)
        .media(media)
        .comments(0)
        .votes(0)
        .reports(0)
        .commented(false)
        .created(DateTime.now())
        .build();

    return BlogEntity.builder().blog(blog).shards(shardMap.values()).build();
  }
}
