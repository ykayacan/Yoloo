package com.yoloo.android.util;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.bluelinelabs.conductor.Controller;

public final class ShareUtil {

  public static void share(Controller controller, @Nullable String shareBodyTitle,
      @NonNull String shareBodyText) {
    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
        TextUtils.isEmpty(shareBodyTitle) ? "" : shareBodyTitle);
    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
    controller.startActivity(Intent.createChooser(intent, "Choose sharing method"));
  }
}
