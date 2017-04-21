package com.yoloo.android;

import com.yoloo.android.util.FormUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FormUtilTest {

  @Test public void addition_isCorrect() throws Exception {
    assertEquals(4, 2 + 2);
  }

  @Test public void isValidUrl() throws Exception {
    System.out.printf("?" + FormUtil.isWebUrl("https:appspot.com"));
    assertFalse(FormUtil.isWebUrl("https:appspot.com"));
  }
}
