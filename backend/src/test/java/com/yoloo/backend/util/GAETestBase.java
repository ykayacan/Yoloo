package com.yoloo.backend.util;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.TimeZone;

@RunWith(JUnit4.class)
public abstract class GAETestBase {

    protected LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    // Set no eventual consistency, that way queries return all results.
                    // http://g.co/cloud/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
                    new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
                    new LocalUserServiceTestConfig(),
                    new LocalURLFetchServiceTestConfig(),
                    new LocalSearchServiceTestConfig(),
                    new LocalMemcacheServiceTestConfig()
            )
                    .setTimeZone(TimeZone.getDefault())
                    .setEnvAttributes(ImmutableMap.<String, Object>of("com.google.appengine.api.users.UserService.user_id_key", "agR0ZXN0cg0LEgdBY2NvdW50GAEM"));

    @Before
    public void setUpGAE() {
        this.helper.setUp();
    }

    @After
    public void tearDownGAE() {
        this.helper.tearDown();
    }
}