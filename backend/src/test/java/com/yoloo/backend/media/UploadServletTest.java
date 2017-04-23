package com.yoloo.backend.media;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;

@RunWith(JUnit4.class)
public class UploadServletTest extends TestBase {

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
        fact().register(MediaEntity.class);

        Key<Account> ownerKey = fact().allocateId(Account.class);
    }

    @Test
    public void testUploadMedia() throws Exception {

    }
}
