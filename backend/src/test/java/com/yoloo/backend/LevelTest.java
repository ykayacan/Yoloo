package com.yoloo.backend;

import com.yoloo.backend.game.level.Level;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelTest {

  @Test public void testFindLevelByPoints1() throws Exception {
    assertEquals(1, Level.findLevelForPoint(350));
  }

  @Test public void testFindLevelByPoints2() throws Exception {
    assertEquals(1, Level.findLevelForPoint(499));
  }

  @Test public void testFindLevelByPoints3() throws Exception {
    assertEquals(2, Level.findLevelForPoint(500));
  }

  @Test public void testFindLevelByPoints4() throws Exception {
    assertEquals(2, Level.findLevelForPoint(550));
  }
}
