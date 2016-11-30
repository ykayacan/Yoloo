package com.yoloo.backend.vote;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentCounterShard;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.util.TestBase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class VoteControllerTest extends TestBase {

    private static final String USER_EMAIL = "test@gmail.com";
    private static final String USER_AUTH_DOMAIN = "gmail.com";

    private Account owner;
    private Question question;

    @Override
    public void setUpGAE() {
        super.setUpGAE();

        helper.setEnvIsLoggedIn(true)
                .setEnvIsAdmin(true)
                .setEnvAuthDomain(USER_AUTH_DOMAIN)
                .setEnvEmail(USER_EMAIL);
    }

    @Override
    public void setUp() {
        super.setUp();

        fact().register(Account.class);
        fact().register(Comment.class);
        fact().register(CommentCounterShard.class);
        fact().register(Question.class);
        fact().register(QuestionCounterShard.class);
        fact().register(Vote.class);

        Key<Account> ownerKey = ofy().factory().allocateId(Account.class);
        Key<Question> questionKey = ofy().factory().allocateId(ownerKey, Question.class);

        owner = createAccount(ownerKey);
        question = createQuestion(ownerKey, questionKey);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        builder.add(owner)
                .add(question)
                .addAll(QuestionShardService.newInstance().createShards(questionKey));

        ofy().save().entities(builder.build()).now();
    }

    @Test
    public void testVoteComment_Up() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.UP, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.UP, comment.getDir());
        assertEquals(1, comment.getVotes());
    }

    @Test
    public void testVoteComment_Down() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DOWN, comment.getDir());
        assertEquals(-1, comment.getVotes());
    }

    @Test
    public void testVoteComment_Default() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());
    }

    @Test
    public void testVoteComment_UpToDefault() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.UP, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.UP, comment.getDir());
        assertEquals(1, comment.getVotes());

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());
    }

    @Test
    public void testVoteComment_DefaultToUp() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.UP, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.UP, comment.getDir());
        assertEquals(1, comment.getVotes());
    }

    @Test
    public void testVoteComment_DefaultToDown() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DOWN, comment.getDir());
        assertEquals(-1, comment.getVotes());
    }

    @Test
    public void testVoteComment_DownToDefault() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        Comment comment = getCommentController()
                .add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DOWN, comment.getDir());
        assertEquals(-1, comment.getVotes());

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

        comment = CommentUtil.aggregateVote(Key.<Account>create(user.getUserId()), comment);
        comment = CommentUtil.aggregateCounts(comment, CommentShardService.newInstance());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());
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

    private Question createQuestion(Key<Account> userKey, Key<Question> postKey) {
        return Question.builder()
                .id(postKey.getId())
                .parentUserKey(userKey)
                .acceptedComment(null)
                .avatarUrl(new Link("test"))
                .username("testUser")
                .firstComment(false)
                .bounty(0)
                .comments(0)
                .votes(0)
                .content("demo content")
                .dir(Vote.Direction.DEFAULT)
                .created(DateTime.now())
                .build();
    }

    private CommentController getCommentController() {
        return CommentController
                .newInstance(
                        CommentService.newInstance(),
                        CommentShardService.newInstance(),
                        QuestionService.newInstance(),
                        QuestionShardService.newInstance(),
                        GamificationService.newInstance(),
                        NotificationService.newInstance());
    }

    private VoteController getVoteController() {
        return VoteController.newInstance(
                QuestionShardService.newInstance(),
                CommentShardService.newInstance()
        );
    }
}
