package com.yoloo.backend.game;

import com.yoloo.backend.game.level.Level;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelTest extends TestBase {

  @Test public void testCorrectLevel_0() {
    final int point = 125;

    assertEquals(0, Level.findLevelForPoint(point));
  }

  @Test public void testCorrectLevel_1() {
    final int point = 304;

    assertEquals(1, Level.findLevelForPoint(point));
  }

  @Test public void testCorrectLevel_2() {
    final int point = 999;

    assertEquals(2, Level.findLevelForPoint(point));
  }

  @Test public void testCorrectLevel_3() {
    final int point = 1500;

    assertEquals(3, Level.findLevelForPoint(point));
  }

  @Test public void testCorrectLevel_4() {
    final int point = 2780;

    assertEquals(4, Level.findLevelForPoint(point));
  }

  @Test public void testCorrectLevel_5() {
    final int point = 5005;

    assertEquals(5, Level.findLevelForPoint(point));
  }
}
