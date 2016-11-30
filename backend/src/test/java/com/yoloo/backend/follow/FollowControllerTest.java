package com.yoloo.backend.follow;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.account.AccountUtil;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FollowControllerTest extends TestBase {

    private static final String USER_EMAIL = "test@gmail.com";
    private static final String USER_AUTH_DOMAIN = "gmail.com";

    private Account follower;
    private Account following;

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
        fact().register(AccountCounterShard.class);
        fact().register(Follow.class);

        Key<Account> followerKey = ofy().factory().allocateId(Account.class);
        Key<Account> followingKey = ofy().factory().allocateId(Account.class);

        follower = Account.builder()
                .id(followerKey.getId())
                .build();

        following = Account.builder()
                .id(followingKey.getId())
                .build();

        List<AccountCounterShard> followerShards =
                AccountShardService.newInstance().createShards(followerKey);
        List<AccountCounterShard> followingShards =
                AccountShardService.newInstance().createShards(followingKey);

        ofy().defer().save().entities(followerShards);
        ofy().defer().save().entities(followingShards);
        ofy().defer().save().entities(follower, following);
    }

    @Test
    public void testFollowAccount() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        FollowController controller = getFollowController();

        AccountShardService service = AccountShardService.newInstance();

        controller.follow(following.getKey().toWebSafeString(), user);

        assertEquals(1, AccountUtil.aggregateCounts(follower, service).getFollowings());
        assertEquals(1, AccountUtil.aggregateCounts(following, service).getFollowers());

        Follow follow = ofy().load().type(Follow.class)
                .ancestor(follower.getKey()).first().now();

        assertEquals(follower.getKey(), follow.getParentUserKey());
        assertEquals(following.getKey(), follow.getFollowingKey());

        assertEquals(1, ofy().load().type(Follow.class).ancestor(follower.getKey()).count());
    }

    @Test(expected = NotFoundException.class)
    public void testUnfollowAccount() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        FollowController controller = getFollowController();

        AccountShardService service = AccountShardService.newInstance();

        controller.follow(following.getKey().toWebSafeString(), user);

        controller.unfollow(following.getKey().toWebSafeString(), user);

        assertEquals(0, AccountUtil.aggregateCounts(follower, service).getFollowings());
        assertEquals(0, AccountUtil.aggregateCounts(following, service).getFollowers());

        ofy().load().type(Follow.class).ancestor(follower.getKey()).first().safe();
    }

    private FollowController getFollowController() {
        return FollowController.newInstance(
                FollowService.newInstance(),
                AccountShardService.newInstance(),
                NotificationService.newInstance()
        );
    }
}
