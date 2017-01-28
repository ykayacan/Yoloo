package com.yoloo.backend.question;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.GamePointNotification;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.tag.TagCounterShard;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.topic.CategoryShardService;
import com.yoloo.backend.topic.TopicCounterShard;
import com.yoloo.backend.validator.Guard;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.util.StringUtil.splitToSet;

@AllArgsConstructor(staticName = "create")
public final class QuestionController extends Controller {

  private static final Logger logger =
      Logger.getLogger(QuestionController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private QuestionService questionService;

  private QuestionShardService questionShardService;

  private CommentService commentService;

  private CommentShardService commentShardService;

  private TagShardService tagShardService;

  private CategoryShardService categoryShardService;

  private AccountService accountService;

  private AccountShardService accountShardService;

  private GamificationService gameService;

  private MediaService mediaService;

  private NotificationService notificationService;

  /**
   * Get question.
   *
   * @param questionId the websafe question id
   * @param user the user
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public Question get(String questionId, User user) throws NotFoundException {
    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    // Create question key from websafe id.
    final Key<Question> questionKey = Key.create(questionId);

    // Fetch question.
    Question question = ofy().load()
        .group(Question.ShardGroup.class).key(questionKey).now();

    Guard.checkNotFound(question, "Could not find question with ID: " + questionId);

    return QuestionUtil
        .mergeCounts(question)
        .flatMap(question1 -> QuestionUtil.mergeVoteDirection(question1, accountKey, true))
        .blockingSingle();
  }

  /**
   * Add question.
   *
   * @param content the content
   * @param tags the tags
   * @param categories the categories
   * @param mediaId the media id
   * @param bounty the bounty
   * @param user the user
   * @return the question
   */
  public Question add(String content, String tags, String categories, Optional<String> mediaId,
      Optional<Integer> bounty, User user) {
    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    keyBuilder.add(accountKey);

    // Get a random shard key.
    final Key<AccountCounterShard> accountShardKey =
        accountShardService.getRandomShardKey(accountKey);
    keyBuilder.add(accountShardKey);

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
    AccountCounterShard accountCounterShard = (AccountCounterShard) fetched.get(accountShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    Media media = (Media) fetched.get(mediaId.isPresent() ? Key.create(mediaId.get()) : null);

    // Create a new question from given inputs.
    QuestionModel model =
        questionService.create(account, content, tags, categories, bounty.or(0), media, tracker);

    Question question = model.getQuestion();
    List<QuestionCounterShard> shards = model.getShards();

    // Increase total question count.
    accountShardService.updateCounter(accountCounterShard, AccountShardService.Update.POST_UP);
    Collection<TagCounterShard> tagShards =
        tagShardService.updateShards(question.getTags());
    Collection<TopicCounterShard> categoryShards =
        categoryShardService.updateShards(question.getCategories());

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
        .add(question)
        .addAll(shards)
        .addAll(tagShards)
        .addAll(categoryShards)
        .add(accountCounterShard)
        .add(tracker)
        .build();

    ofy().transact(() -> ofy().save().entities(saveList).now());

    FeedUpdateServlet.addToQueue(user.getUserId(), question.getWebsafeId());

    return question;
  }

  /**
   * Update question.
   *
   * @param questionId the question id
   * @param content the content
   * @param bounty the bounty
   * @param tags the tags
   * @param mediaId the media id
   * @param user the user
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public Question update(String questionId, Optional<String> content, Optional<Integer> bounty,
      Optional<String> tags, Optional<String> mediaId, User user) throws NotFoundException {
    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create question key from websafe question id.
    final Key<Question> questionKey = Key.create(questionId);
    keyBuilder.add(questionKey);

    if (mediaId.isPresent()) {
      Key<Media> mediaKey = Key.create(mediaId.get());
      keyBuilder.add(mediaKey);
      // Deletes existing media item from cloud storage.
      mediaService.delete(mediaKey);
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Question question = (Question) fetched.get(questionKey);
    Guard.checkNotFound(question, "Could not find question with ID: " + questionId);

    //noinspection SuspiciousMethodCalls
    Media media = (Media) fetched
        .get(mediaId.isPresent() ? Key.<Media>create(mediaId.get()) : null);

    question =
        questionService.update(question, Optional.fromNullable(media), content, bounty, tags);

    ofy().save().entity(question).now();

    return question;
  }

  /**
   * Remove question.
   *
   * @param questionId the websafe question id
   * @param user the user
   * @throws NotFoundException the not found exception
   */
  public void delete(String questionId, User user) throws NotFoundException {
    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create question key from websafe id.
    final Key<Question> questionKey = Key.create(questionId);

    Guard.checkNotFound(questionKey, "Could not find question with ID: " + questionId);

    Key<AccountCounterShard> shardKey = accountShardService.getRandomShardKey(accountKey);
    AccountCounterShard shard = ofy().load().key(shardKey).now();
    shard.decreaseQuestions();

    // TODO: 27.11.2016 Change this operations to a push queue.

    List<Key<Comment>> commentKeys = questionService.getCommentKeys(questionKey);

    ofy().transact(() -> {
      ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
          .addAll(commentShardService.createShardKeys(commentKeys))
          .addAll(commentService.getVoteKeys(commentKeys))
          .addAll(commentKeys)
          .addAll(questionService.getVoteKeys(questionKey))
          .addAll(questionShardService.createShardKeys(questionKey))
          .add(questionKey)
          .build();

      ofy().defer().save().entity(shard);
      ofy().defer().delete().keys(deleteList);
    });
  }

  /**
   * List collection response.
   *
   * @param sorter the sorter
   * @param category the category
   * @param limit the limit
   * @param cursor the cursor
   * @param user the user    @return the collection response
   */
  public CollectionResponse<Question> list(Optional<QuestionSorter> sorter,
      Optional<String> category, Optional<String> tags, Optional<Integer> limit,
      Optional<String> cursor, User user) {

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // If sorter parameter is null, default sort strategy is "NEWEST".
    QuestionSorter questionSorter = sorter.or(QuestionSorter.NEWEST);

    // Init query fetch request.
    Query<Question> query = ofy().load().type(Question.class);

    if (category.isPresent()) {
      Set<String> categorySet = splitToSet(category.get(), ",");

      for (String categoryName : categorySet) {
        query = query.filter(Question.FIELD_CATEGORIES + " =", categoryName);
      }
    } else if (tags.isPresent()) {
      Set<String> tagSet = splitToSet(tags.get(), ",");

      for (String tagName : tagSet) {
        query = query.filter(Question.FIELD_TAGS + " =", tagName);
      }
    }

    // Sort by post sorter then edit query.
    query = QuestionSorter.sort(query, questionSorter);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Question> qi = query.iterator();

    List<Question> questions = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      questions.add(qi.next());
    }

    questions = QuestionUtil.mergeCounts(questions)
        .toList(DEFAULT_LIST_LIMIT)
        .flatMap(questions1 -> QuestionUtil
            .mergeVoteDirection(questions1, accountKey, false).toList())
        .blockingGet();

    return CollectionResponse.<Question>builder()
        .setItems(questions)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public CollectionResponse<Question> list(String accountId, Optional<Integer> limit,
      Optional<String> cursor, User user) {

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(accountId);

    // Init query fetch request.
    Query<Question> query = ofy().load().type(Question.class)
        .ancestor(accountKey);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Question> qi = query.iterator();

    List<Question> questions = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      questions.add(qi.next());
    }

    questions = QuestionUtil.mergeCounts(questions)
        .toList(DEFAULT_LIST_LIMIT)
        .flatMap(questions1 -> QuestionUtil
            .mergeVoteDirection(questions1, accountKey, false).toList())
        .blockingGet();

    return CollectionResponse.<Question>builder()
        .setItems(questions)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  /**
   * Report.
   *
   * @param questionId the websafe question id
   * @param user the user
   */
  public void report(String questionId, User user) throws NotFoundException {
    // Create question key from websafe question id.
    final Key<Question> questionKey = Key.create(questionId);

    final Key<Account> reporterKey = Key.create(user.getUserId());

    Question question = ofy().load().key(questionKey).now();

    Guard.checkNotFound(question, "Could not find question with ID: " + questionId);

    question.toBuilder().reportedByKey(reporterKey).build();

    ofy().save().entity(question);
  }
}