package com.yoloo.android.util;

import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.SignInButton;

public final class LoginHelper {

  public static void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
    int count = signInButton.getChildCount();
    for (int i = 0; i < count; i++) {
      final View v = signInButton.getChildAt(i);
      if (v instanceof TextView) {
        TextView tv = (TextView) v;
        tv.setText(buttonText);
        break;
      }
    }
  }
}
