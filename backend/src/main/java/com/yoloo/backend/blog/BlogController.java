package com.yoloo.backend.blog;

import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.GamePointNotification;
import com.yoloo.backend.question.FeedUpdateServlet;
import com.yoloo.backend.tag.TagCounterShard;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.topic.CategoryShardService;
import com.yoloo.backend.topic.TopicCounterShard;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class BlogController {

  private static final Logger logger =
      Logger.getLogger(BlogController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private BlogService blogService;

  private CommentService commentService;

  private CommentShardService commentShardService;

  private TagShardService tagShardService;

  private CategoryShardService categoryShardService;

  private AccountService accountService;

  private AccountShardService accountShardService;

  private GamificationService gameService;

  private MediaService mediaService;

  private NotificationService notificationService;

  public Blog add(String title, String content, String tags, String categories,
      Optional<String> mediaId, User user) {
    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    keyBuilder.add(accountKey);

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(accountKey);
    keyBuilder.add(recordKey);

    if (mediaId.isPresent()) {
      final Key<Media> mediaKey = Key.create(mediaId.get());
      keyBuilder.add(mediaKey);
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    Media media = (Media) fetched.get(mediaId.isPresent() ? Key.create(mediaId.get()) : null);

    // Create a new question from given inputs.
    BlogEntity entity =
        blogService.create(account, title, content, tags, categories, media, tracker);

    Blog blog = entity.getBlog();
    Collection<BlogCounterShard> shards = entity.getShards();

    Collection<TagCounterShard> tagShards =
        tagShardService.updateShards(blog.getTags());
    Collection<TopicCounterShard> categoryShards =
        categoryShardService.updateShards(blog.getCategories());

    // Check game elements.
    /*Tracker updatedTracker = GameVerifier.builder()
        .tracker(tracker)
        .rule(FirstQuestionRule.of(tracker))
        .rule(DailyFirstQuestionRule.of(tracker))
        .rule(ValidBountyRule.of(tracker, question))
        .build()
        .verify();*/

    GamePointNotification gamePointNotification = GamePointNotification.create(record, tracker);
    notificationService.send(gamePointNotification);

    // Immutable helper list object to save all entities in a single db write.
    // For each single object use builder.addAdmin() method.
    // For each list object use builder.addAll() method.
    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(blog)
        .addAll(shards)
        .addAll(tagShards)
        .addAll(categoryShards)
        .add(tracker)
        .build();

    ofy().transact(() -> ofy().save().entities(saveList).now());

    FeedUpdateServlet.addToQueue(user.getUserId(), blog.getWebsafeId());

    return blog;
  }
}
