package com.yoloo.backend.bookmark;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;

@RunWith(JUnit4.class)
public class BookmarkControllerTest extends TestBase {

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
        fact().register(AccountCounterShard.class);
        fact().register(Question.class);
        fact().register(QuestionCounterShard.class);
        fact().register(Bookmark.class);

        Key<Account> ownerKey = ofy().factory().allocateId(Account.class);

        Account owner = Account.builder()
                .id(ownerKey.getId())
                .build();

        List<AccountCounterShard> followerShards =
                AccountShardService.newInstance().createShards(ownerKey);


        ofy().defer().save().entities(followerShards);
        ofy().defer().save().entity(owner);
    }

    @Test
    public void testSaveQuestion() throws Exception {
        final User user = UserServiceFactory.getUserService().getCurrentUser();


    }
}
