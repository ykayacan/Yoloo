package com.yoloo.backend.util;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;

abstract class GAETestBase {

  protected LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // http://g.co/cloud/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
          new LocalUserServiceTestConfig(),
          new LocalURLFetchServiceTestConfig(),
          new LocalTaskQueueTestConfig()
              .setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml"),
          new LocalSearchServiceTestConfig(),
          new LocalMemcacheServiceTestConfig()
      )
          .setTimeZone(TimeZone.getDefault())
          .setEnvAttributes(ImmutableMap.<String, Object>of(
              "com.google.appengine.api.users.UserService.user_id_key",
              "agR0ZXN0cg0LEgdBY2NvdW50GAEM"));

  @Before public void setUpGAE() {
    this.helper.setUp();
  }

  @After public void tearDownGAE() {
    this.helper.tearDown();
  }
}