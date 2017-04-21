package com.yoloo.backend;

import com.yoloo.backend.util.ZScore;
import org.junit.Test;

public class ZScoreTest {

  @Test public void deneme() throws Exception {
    int[] test = { 1, 1, 1, 1, 1, 1, 9, 9, 9, 9, 9, 9 };
    ZScore score = new ZScore(0.8, test);

    System.out.println(score.getScore(1));
    //assertEquals(-1.67770595327, score.getScore(1));
  }
}
