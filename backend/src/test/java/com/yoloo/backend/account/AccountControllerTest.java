package com.yoloo.backend.account;

import com.yoloo.backend.util.TestBase;
import org.junit.Test;

public class AccountControllerTest extends TestBase {

  @Test public void testRandomUsernameFromRealname() {
    String realname = "Yasin Sinan Kayacan";
    long millis = System.currentTimeMillis();

    String username = realname.trim().replaceAll("\\s+", "")
        .concat(String.valueOf(millis).substring(5));

    System.out.println("Username: " + username);
  }
}
