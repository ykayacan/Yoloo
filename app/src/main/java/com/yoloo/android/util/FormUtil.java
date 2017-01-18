package com.yoloo.android.util;

import android.util.Patterns;

public final class FormUtil {

  public static boolean isEmailValid(String email) {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }

  public static boolean isPasswordValid(String password) {
    return password.length() > 4;
  }
}
