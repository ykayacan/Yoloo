package com.yoloo.android.feature.ui.widget.zamanview;

import java.util.Date;
import java.util.HashMap;

public class ZamanManager {

  private static final HashMap<String, Integer> UNITS = new HashMap<>();

  private static ZamanManager instance = null;

  public static ZamanManager getInstance(long timeStamp) {
    if (instance == null) {
      instance = new ZamanManager(timeStamp);
    }
    return instance;
  }

  static {
    UNITS.put("minute", 60);
    UNITS.put("hour", 3600);
    UNITS.put("day", 86400);
    UNITS.put("week", 604800);
    UNITS.put("month", 2592000);
    UNITS.put("year", 31104000);
  }

  private String time;

  private ZamanManager(long timeStamp) {
    this.calculateTime(timeStamp);
  }

  private static long getCurrentTimeStamp() {
    return new Date().getTime() / 1000;
  }

  public void calculateTime(long timeStamp) {
    long diff = getCurrentTimeStamp() - timeStamp;
    diffToString(diff);
  }

  public String getTime() {
    return time;
  }

  private void getTimeString(long diff, String oneUnit, String oneThing, String manyThings) {
    int unit = UNITS.get(oneUnit);
    if (diff < 0) {
      //FUTURE
      diff = Math.abs(diff);
      if (diff < 2 * unit) {
        time = ZamanTimeString.IN_ONE_THING.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        time = String.valueOf(numberOfThings) + manyThings;
      }
    } else {
      //PAST
      if (diff < 2 * unit) {
        time = ZamanTimeString.ONE_THING_AGO.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        time = String.valueOf(numberOfThings) + manyThings;
      }
    }
  }

  private void diffToString(long diff) {
    long absDiff = Math.abs(diff);
    if (absDiff < UNITS.get("minute")) {
      //NOW
      if (diff < 0) {
        time = ZamanTimeString.IN_FEW_SECONDS;
      } else {
        time = ZamanTimeString.NOW;
      }
    } else if (absDiff < UNITS.get("hour")) {
      //MINS
      getTimeString(diff, "minute", ZamanTimeString.MINUTE, ZamanTimeString.MINUTES);
    } else if (absDiff < UNITS.get("day")) {
      //HOURS
      getTimeString(diff, "hour", ZamanTimeString.HOUR, ZamanTimeString.HOURS);
    } else if (absDiff < UNITS.get("week")) {
      //DAYS
      getTimeString(diff, "day", ZamanTimeString.DAY, ZamanTimeString.DAYS);
    } else if (absDiff < UNITS.get("month")) {
      //WEEKS
      getTimeString(diff, "week", ZamanTimeString.WEEK, ZamanTimeString.WEEKS);
    } else if (absDiff < UNITS.get("year")) {
      //MONTHS
      getTimeString(diff, "month", ZamanTimeString.MONTH, ZamanTimeString.MONTHS);
    } else {
      //YEARS
      getTimeString(diff, "year", ZamanTimeString.YEAR, ZamanTimeString.YEARS);
    }
  }
}
