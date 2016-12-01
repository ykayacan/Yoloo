package com.yoloo.backend.comment;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.question.QuestionUtil;
import com.yoloo.backend.util.TestBase;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CommentControllerTest extends TestBase {

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

        QuestionShardService shardService = QuestionShardService.newInstance();

        owner = createAccount(ownerKey);
        question = createQuestion(ownerKey, questionKey, shardService);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        builder.add(owner)
                .add(question)
                .addAll(shardService.createShards(questionKey));

        ofy().save().entities(builder.build()).now();
    }

    @Test
    public void testAddComment() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment comment = controller.add(question.getWebsafeId(), "Test comment", user);

        assertEquals("Test comment", comment.getContent());
        assertEquals(Ref.create(Key.create(user.getUserId())), comment.getParentUser());
        assertEquals(owner.getUsername(), comment.getUsername());
        assertEquals(owner.getAvatarUrl(), comment.getAvatarUrl());
        assertEquals(Vote.Direction.DEFAULT, comment.getDir());
        assertEquals(0, comment.getVotes());
        assertEquals(false, comment.isAccepted());

        Question question = ofy().load().key(comment.getQuestionKey()).now();
        question = QuestionUtil.aggregateCounts(question);

        assertEquals(1, question.getComments());
    }

    @Test
    public void testAddComment_firstComment() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment comment = controller.add(question.getWebsafeId(), "Test comment", user);

        Question question = ofy().load().key(comment.getQuestionKey()).now();
        assertEquals(true, question.isFirstComment());
    }

    @Test
    public void testUpdateComment_contentChanged() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment original = controller.add(question.getWebsafeId(), "Test comment", user);

        Comment changed = controller.update(
                question.getWebsafeId(),
                original.getWebsafeId(),
                Optional.of("Hello Yoloo"),
                Optional.<Boolean>absent(),
                user);

        assertEquals("Hello Yoloo", changed.getContent());
        assertEquals(original.isAccepted(), changed.isAccepted());
    }

    @Test
    public void testUpdateComment_acceptedChanged() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment original = controller.add(question.getWebsafeId(), "Test comment", user);

        Comment changed = controller.update(
                question.getWebsafeId(),
                original.getWebsafeId(),
                Optional.<String>absent(),
                Optional.of(true),
                user);

        assertEquals(original.getContent(), changed.getContent());
        assertEquals(true, changed.isAccepted());

        Question question = ofy().load().key(changed.getQuestionKey()).now();
        assertEquals(Ref.create(changed.getKey()), question.getAcceptedComment());
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveComment() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment comment = controller.add(question.getWebsafeId(), "Test comment", user);

        controller.delete(question.getWebsafeId(), comment.getWebsafeId(), user);

        ofy().load().key(comment.getKey()).safe();
    }

    @Test
    public void testRemoveComment_removeArtifacts() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        CommentController controller = getCommentController();

        Comment comment = controller.add(question.getWebsafeId(), "Test comment", user);

        getVoteController().vote(comment.getWebsafeId(), Vote.Direction.UP, user);

        controller.delete(question.getWebsafeId(), comment.getWebsafeId(), user);

        assertEquals(null, ofy().load().key(comment.getKey()).now());

        assertEquals(0, ofy().load().keys(comment.getShardKeys()).size());

        ImmutableList<Key<Vote>> voteKeys =
                CommentService.newInstance().getVoteKeys(comment.getKey());
        assertEquals(0, ofy().load().keys(voteKeys).size());
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

    private Question createQuestion(Key<Account> userKey, Key<Question> questionKey,
                                    QuestionShardService service) {
        return Question.builder()
                .id(questionKey.getId())
                .parentUserKey(userKey)
                .acceptedComment(null)
                .avatarUrl(new Link("test"))
                .username("testUser")
                .firstComment(false)
                .shardKeys(service.createShardKeys(questionKey))
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
