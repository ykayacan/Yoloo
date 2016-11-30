package com.yoloo.backend.base;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.TimeZone;

@RunWith(JUnit4.class)
public abstract class GaeTestBase {

    protected final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    // Set no eventual consistency, that way queries return all results.
                    // http://g.co/cloud/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
                    new LocalDatastoreServiceTestConfig()
                            .setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
                    new LocalUserServiceTestConfig(),
                    new LocalURLFetchServiceTestConfig(),
                    new LocalSearchServiceTestConfig(),
                    new LocalMemcacheServiceTestConfig()
            )
                    .setTimeZone(TimeZone.getDefault())
                    .setEnvAttributes(ImmutableMap.<String, Object>of("com.google.appengine.api.users.UserService.user_id_key", "agR0ZXN0cg0LEgdBY2NvdW50GAEM"));

    private Closeable dbSession;

    @BeforeClass
    public static void setUpBeforeClass() {
        // Reset the Factory so that all translators work properly.
        ObjectifyService.setFactory(new ObjectifyFactory());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        dbSession = ObjectifyService.begin();

        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();

        registerClasses(builder);

        for (Class<?> clazz : builder.build()) {
            ObjectifyService.register(clazz);
        }
    }

    @After
    public void tearDown() throws Exception {
        dbSession.close();
        helper.tearDown();
    }

    protected abstract void registerClasses(ImmutableList.Builder<Class<?>> builder);
}
