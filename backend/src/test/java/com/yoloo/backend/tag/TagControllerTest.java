package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.config.ShardConfig;
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
public class TagControllerTest extends TestBase {

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
        fact().register(Tag.class);
        fact().register(TagCounterShard.class);
        fact().register(TagGroup.class);
    }

    @Test
    public void testAddGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup group = getTagController().addGroup("test group", user);

        assertEquals("test group", group.getName());
        assertEquals(0, group.getTotalQuestionCount());
        assertEquals(0, group.getTotalTagCount());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup original = getTagController().addGroup("test group", user);

        TagGroup updated = getTagController()
                .updateGroup(original.getWebsafeId(), Optional.of("test group 2"), user);

        assertEquals(original.getKey(), updated.getKey());
        assertEquals(original.getTotalTagCount(), updated.getTotalTagCount());
        assertEquals(original.getTotalQuestionCount(), updated.getTotalQuestionCount());
        assertEquals("test group 2", updated.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup original = getTagController().addGroup("test group", user);

        ofy().save().entity(original).now();

        getTagController().deleteGroup(original.getWebsafeId(), user);

        ofy().load().key(original.getKey()).safe();
    }

    @Test
    public void testAddHashTag_singleGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup group = getTagController().addGroup("test group", user);

        Tag tag = getTagController()
                .addTag("h1", Locale.ENGLISH.getISO3Language(), group.getWebsafeId(), user);

        assertEquals("h1", tag.getName());
        assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());
        assertEquals(ShardConfig.HASHTAG_SHARD_COUNTER, tag.getShards().size());

        List<Key<TagGroup>> keys = new ArrayList<>(1);
        keys.add(group.getKey());

        assertEquals(keys, tag.getGroupKeys());
        assertEquals(0, tag.getQuestions());
    }

    @Test
    public void testAddHashTag_multipleGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup group1 = getTagController().addGroup("test group", user);
        TagGroup group2 = getTagController().addGroup("test group 2", user);

        String groupIds = group1.getWebsafeId() + "," + group2.getWebsafeId();

        Tag tag = getTagController()
                .addTag("h1", Locale.ENGLISH.getISO3Language(), groupIds, user);

        assertEquals("h1", tag.getName());
        assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());

        List<Key<TagGroup>> keys = new ArrayList<>(1);
        keys.add(group1.getKey());
        keys.add(group2.getKey());

        assertEquals(keys, tag.getGroupKeys());
        assertEquals(0, tag.getQuestions());
    }

    @Test
    public void testSuggestTags_suggestByTagSimilarity() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup group1 = getTagController().addGroup("travel", user);
        TagGroup group2 = getTagController().addGroup("budget", user);

        Tag cheap = getTagController().addTag("cheap", "en", group2.getWebsafeId(), user);
        Tag lowBudget = getTagController().addTag("low budget", "en", group2.getWebsafeId(), user);

        CollectionResponse<Tag> response =
                getTagController().list(cheap.getName(), Optional.<Integer>absent(), user);

        assertEquals(1, response.getItems().size());
    }

    @Test
    public void testSuggestTags_suggestByGroup() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();

        TagGroup group1 = getTagController().addGroup("travel", user);
        TagGroup group2 = getTagController().addGroup("budget", user);

        Tag femaleTravel = getTagController()
                .addTag("female travel", "en", group1.getWebsafeId(), user);
        Tag camp = getTagController()
                .addTag("camp", "en", group1.getWebsafeId(), user);

        Tag cheap = getTagController().addTag("cheap", "en", group2.getWebsafeId(), user);
        Tag lowBudget = getTagController().addTag("low budget", "en", group2.getWebsafeId(), user);

        CollectionResponse<Tag> response =
                getTagController().list(group1.getName(), Optional.<Integer>absent(), user);

        assertEquals(2, response.getItems().size());
    }

    private TagController getTagController() {
        return TagController.create(
                TagService.create(),
                TagShardService.create()
        );
    }
}
