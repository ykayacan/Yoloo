package com.yoloo.backend.hashTag;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.hashtag.HashTag;
import com.yoloo.backend.hashtag.HashTagController;
import com.yoloo.backend.hashtag.HashTagCounterShard;
import com.yoloo.backend.hashtag.HashTagGroup;
import com.yoloo.backend.hashtag.HashTagService;
import com.yoloo.backend.hashtag.HashTagShardService;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class HashTagControllerTest extends TestBase {

    private static final String USER_EMAIL = "test@gmail.com";
    private static final String USER_AUTH_DOMAIN = "gmail.com";

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
        fact().register(HashTag.class);
        fact().register(HashTagCounterShard.class);
        fact().register(HashTagGroup.class);

        Key<Account> userKey = ofy().factory().allocateId(Account.class);
    }

    @Test
    public void testAddGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        HashTagGroup group = getHashTagController().addGroup("test group", user);

        assertEquals("test group", group.getName());
        assertEquals(0, group.getTotalQuestionCount());
        assertEquals(0, group.getTotalHashTagCount());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        HashTagGroup original = getHashTagController().addGroup("test group", user);

        HashTagGroup updated = getHashTagController()
                .updateGroup(original.getWebsafeId(), Optional.of("test group 2"), user);

        assertEquals(original.getKey(), updated.getKey());
        assertEquals(original.getTotalHashTagCount(), updated.getTotalHashTagCount());
        assertEquals(original.getTotalQuestionCount(), updated.getTotalQuestionCount());
        assertEquals("test group 2", updated.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        HashTagGroup original = getHashTagController().addGroup("test group", user);

        ofy().save().entity(original).now();

        getHashTagController().deleteGroup(original.getWebsafeId(), user);

        ofy().load().key(original.getKey()).safe();
    }

    @Test
    public void testAddHashTag_singleGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        HashTagGroup group = getHashTagController().addGroup("test group", user);

        HashTag hashTag = getHashTagController()
                .addHashTag("h1", Locale.ENGLISH.getISO3Language(), group.getWebsafeId(), user);

        assertEquals("h1", hashTag.getName());
        assertEquals(Locale.ENGLISH.getISO3Language(), hashTag.getLanguage());

        List<Key<HashTagGroup>> keys = new ArrayList<>(1);
        keys.add(group.getKey());

        assertEquals(keys, hashTag.getGroupKeys());
        assertEquals(0, hashTag.getQuestions());
    }

    @Test
    public void testAddHashTag_multipleGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        HashTagGroup group1 = getHashTagController().addGroup("test group", user);
        HashTagGroup group2 = getHashTagController().addGroup("test group 2", user);

        String groupIds = group1.getWebsafeId() + "," + group2.getWebsafeId();

        HashTag hashTag = getHashTagController()
                .addHashTag("h1", Locale.ENGLISH.getISO3Language(), groupIds, user);

        assertEquals("h1", hashTag.getName());
        assertEquals(Locale.ENGLISH.getISO3Language(), hashTag.getLanguage());

        List<Key<HashTagGroup>> keys = new ArrayList<>(1);
        keys.add(group1.getKey());
        keys.add(group2.getKey());

        assertEquals(keys, hashTag.getGroupKeys());
        assertEquals(0, hashTag.getQuestions());
    }

    private HashTagController getHashTagController() {
        return HashTagController.newInstance(
                HashTagService.newInstance(),
                HashTagShardService.newInstance()
        );
    }
}
