package com.yoloo.android.data.faker;

import android.support.annotation.NonNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

public class FakerUtil {

  @NonNull
  public static String getMediaUrl() {
    return "http://filledpassport.com/wp-content/uploads/2016/05/best-travel-blogs-200x200.png";
  }

  @NonNull
  public static String getContent() {
    return "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
        + "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
        + " when an unknown printer took a galley of type and scrambled "
        + "it to make a type specimen book.";
  }

  public static int generateNumber() {
    return new Random().nextInt(3250);
  }

  public static String getAvatarRandomUrl() {
    int rand = new Random().nextInt(85);
    return "https://randomuser.me/api/portraits/thumb/men/" + rand + ".jpg";
  }

  public static Date getRandomDate() {
    final long beginTime = Timestamp.valueOf("2017-01-13 00:00:00").getTime();
    final long endTime = Timestamp.valueOf("2017-01-14 00:58:00").getTime();
    long diff = endTime - beginTime + 1;

    long timeStamp = beginTime + (long) (Math.random() * diff);
    return new Date(timeStamp);
  }
}
