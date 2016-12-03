package com.yoloo.backend.question;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.GaeTestBase;
import com.yoloo.backend.category.CategoryService;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.tag.HashTagService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class QuestionControllerTest extends GaeTestBase {

    private static final String USER_EMAIL = "test@gmail.com";
    private static final String USER_AUTH_DOMAIN = "gmail.com";

    @Mock
    HttpServletRequest request;

    private Key<Account> userKey;

    private Key<Question> questionKey;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        helper.setEnvIsLoggedIn(true)
                .setEnvAuthDomain(USER_AUTH_DOMAIN)
                .setEnvEmail(USER_EMAIL);

        userKey = ofy().factory().allocateId(Account.class);
        questionKey = ofy().factory().allocateId(userKey, Question.class);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        builder.add(createQuestion(userKey, questionKey))
                .add(createAccount(userKey))
                .add(createVote(userKey, questionKey))
                .addAll(AccountShardService.newInstance().createShards(userKey));

        for (int i = 1; i <= QuestionCounterShard.SHARD_COUNT; i++) {
            builder.add(createShard(questionKey, i));
        }

        ofy().save().entities(builder.build()).now();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void registerClasses(ImmutableList.Builder<Class<?>> builder) {
        builder.add(Question.class)
                .add(Account.class)
                .add(QuestionCounterShard.class)
                .add(AccountCounterShard.class)
                .add(Tag.class)
                .add(Vote.class);
    }

    @Test
    public void testGetQuestion() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        QuestionController controller = getForumController();

        Question question = controller.get(questionKey.toWebSafeString(), user);

        assertEquals(questionKey, question.getKey());
        assertEquals("demo content", question.getContent());
        assertEquals(0, question.getBounty());
        assertEquals(null, question.getAcceptedComment());
        assertEquals(true, question.isFirstComment());
        assertEquals("testUser", question.getUsername());
        assertEquals(new Link("test"), question.getAvatarUrl());
        assertEquals(Vote.Direction.UP, question.getDir());
        assertEquals(100, question.getComments());
        assertEquals(0, question.getReports());
        assertEquals(50, question.getVotes());
    }

    @Test
    public void testAddQuestion() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        QuestionController controller = getForumController();

        HttpServletRequest request = mock(HttpServletRequest.class);

        QuestionWrapper wrapper = QuestionWrapper.builder()
                .content("Test content")
                .hashTags("visa,passport")
                .bounty(10)
                .request(request)
                .build();

        Question question = controller.add(wrapper, user);

        assertEquals("Test content", question.getContent());
        assertEquals(10, question.getBounty());

        Set<String> hashTags = new HashSet<>();
        hashTags.add("visa");
        hashTags.add("passport");

        assertEquals(hashTags, question.getHashTags());
        assertEquals(null, question.getAcceptedComment());
        assertEquals(false, question.isFirstComment());
        assertEquals("Test user", question.getUsername());
        assertEquals(new Link("Test avatar"), question.getAvatarUrl());
        assertEquals(Vote.Direction.DEFAULT, question.getDir());
    }

    @Test
    public void testListQuestions_defaultOrder() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        QuestionController controller = getForumController();

        CollectionResponse<Question> response = controller.list(
                Optional.<QuestionSorter>absent(),
                Optional.<Integer>absent(),
                Optional.<String>absent(),
                user);

        assertTrue(!response.getItems().isEmpty());

        for (Question post : response.getItems()) {
            assertEquals(post.getComments(), 100);
            assertEquals(post.getVotes(), 50);
            assertEquals(post.getReports(), 0);
            assertTrue(post.isFirstComment());
        }
    }

    private Account createAccount(Key<Account> userKey) {
        return Account.builder()
                .id(userKey.getId())
                .avatarUrl(new Link("Test avatar"))
                .email(new Email(USER_EMAIL))
                .username("Test user")
                .created(DateTime.now())
                .build();
    }

    private QuestionCounterShard createShard(Key<Question> postKey, int i) {
        return QuestionCounterShard.builder()
                .id(ShardUtil.generateShardId(postKey, i))
                .comments(20)
                .votes(10)
                .reports(0)
                .build();
    }

    private Question createQuestion(Key<Account> userKey, Key<Question> questionKey) {
        return Question.builder()
                .id(questionKey.getId())
                .parentUserKey(userKey)
                .acceptedComment(null)
                .avatarUrl(new Link("test"))
                .username("testUser")
                .firstComment(true)
                .bounty(0)
                .comments(0)
                .votes(0)
                .content("demo content")
                .dir(Vote.Direction.DEFAULT)
                .created(DateTime.now())
                .build();
    }

    private Vote createVote(Key<Account> userKey, Key<Question> postKey) {
        return Vote.builder()
                .id(postKey.toWebSafeString())
                .parentUserKey(userKey)
                .dir(Vote.Direction.UP)
                .build();
    }

    private QuestionController getForumController() {
        return QuestionController
                .newInstance(
                        QuestionService.newInstance(),
                        QuestionShardService.newInstance(),
                        CommentService.newInstance(),
                        CommentShardService.newInstance(),
                        HashTagService.newInstance(),
                        CategoryService.newInstance(),
                        AccountService.newInstance(),
                        AccountShardService.newInstance(),
                        GamificationService.newInstance(),
                        MediaService.newInstance(),
                        NotificationService.newInstance());
    }
}