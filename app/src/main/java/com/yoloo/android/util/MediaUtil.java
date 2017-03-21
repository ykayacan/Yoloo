package com.yoloo.android.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MediaUtil {

  private MediaUtil() {
    // empty constructor
  }

  public static void addToPhoneGallery(@NonNull String path, Activity activity) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(path);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    activity.sendBroadcast(mediaScanIntent);
  }

  public static String createImageName() {
    return "IMG_" + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(new Date(System.currentTimeMillis())) + ".webp";
  }
}
