package com.yoloo.backend;

import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.algorithm.YolooHotRankAlgorithm.calculate;
import static org.junit.Assert.assertTrue;

public class RedditHotRankTest {

  @Test public void testRedditHotRankTest() {
    DateTime d1 = DateTime.now().minusMinutes(1);
    DateTime d2 = DateTime.now().minusHours(1);

    assertTrue("upvotes good", calculate(10, 2, d1) > calculate(9, 2, d1));
    assertTrue("downvotes bad", calculate(10, 3, d1) < calculate(10, 2, d1));

    assertTrue("freshmeat good", calculate(5, 1, d1) < calculate(5, 1, DateTime.now()));

    assertTrue("age causes decays", calculate(5, 1, d1) > calculate(5, 1, d2));

    assertTrue("faster decay => slightly lower numbers early on",
        calculate(5, 1, d1, 20000) > calculate(5, 1, d1));
  }
}
