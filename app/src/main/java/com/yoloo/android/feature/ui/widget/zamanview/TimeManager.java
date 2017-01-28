package com.yoloo.android.feature.ui.widget.zamanview;

import android.support.v4.util.ArrayMap;
import java.util.Date;

public class TimeManager {

  private static final ArrayMap<String, Integer> UNITS = new ArrayMap<>();

  private static TimeManager instance = null;

  static {
    UNITS.put("m", 60);
    UNITS.put("h", 3600);
    UNITS.put("d", 86400);
    UNITS.put("w", 604800);
    UNITS.put("mo", 2592000);
    UNITS.put("y", 31104000);
  }

  private String time;

  private TimeManager(long timestamp) {
    this.calculateTime(timestamp);
  }

  public static TimeManager getInstance(long timeStamp) {
    if (instance == null) {
      instance = new TimeManager(timeStamp);
    }
    return instance;
  }

  private static long getCurrentTimeStamp() {
    return new Date().getTime() / 1000;
  }

  public void calculateTime(long timeStamp) {
    final long diff = getCurrentTimeStamp() - timeStamp;
    diffToString(diff);
  }

  public String getTime() {
    return time;
  }

  private void diffToString(long diff) {
    final long absDiff = Math.abs(diff);
    if (absDiff < UNITS.get("m")) {
      //NOW
      time = diff < 0 ? TimeConstants.IN_FEW_SECONDS : TimeConstants.NOW;
    } else if (absDiff < UNITS.get("h")) {
      //MINS
      getTimeString(diff, "m", TimeConstants.MINUTES);
    } else if (absDiff < UNITS.get("d")) {
      //HOURS
      getTimeString(diff, "h", TimeConstants.HOURS);
    } else if (absDiff < UNITS.get("w")) {
      //DAYS
      getTimeString(diff, "d", TimeConstants.DAYS);
    } else if (absDiff < UNITS.get("mo")) {
      //WEEKS
      getTimeString(diff, "w", TimeConstants.WEEKS);
    } else if (absDiff < UNITS.get("y")) {
      //MONTHS
      getTimeString(diff, "mo", TimeConstants.MONTHS);
    } else {
      //YEARS
      getTimeString(diff, "y", TimeConstants.YEARS);
    }
  }

  private void getTimeString(long diff, String oneUnit, String manyThings) {
    final int unit = UNITS.get(oneUnit);

    if (diff < 0) {
      //FUTURE
      diff = Math.abs(diff);
      if (diff < 2 * unit) {
        time = TimeConstants.IN_ONE_THING.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        time = String.valueOf(numberOfThings) + manyThings;
      }
    } else {
      //PAST
      if (diff < 2 * unit) {
        time = TimeConstants.ONE_THING_AGO.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        time = String.valueOf(numberOfThings) + manyThings;
      }
    }
  }
}
