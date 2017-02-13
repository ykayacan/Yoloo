package com.yoloo.android.ui.widget.timeview;

import android.support.v4.util.ArrayMap;

class TimeConstants {
  static final String NOW = "now";
  static final String IN_FEW_SECONDS = NOW;

  static final String MINUTE = "m";
  static final String MINUTES = "m";
  static final String ONE_MINUTE_AGO = "1" + MINUTE;
  static final String IN_ONE_MINUTE = ONE_MINUTE_AGO;

  static final String HOUR = "h";
  static final String HOURS = "h";
  static final String ONE_HOUR_AGO = "1" + HOUR;
  static final String IN_ONE_HOUR = ONE_HOUR_AGO;

  static final String DAY = "d";
  static final String DAYS = "d";
  static final String ONE_DAY_AGO = "1d";
  static final String IN_ONE_DAY = "Tomorrow";

  static final String WEEK = "w";
  static final String WEEKS = "w";
  static final String ONE_WEEK_AGO = "1" + WEEK;
  static final String IN_ONE_WEEK = "1" + WEEK;

  static final String MONTH = "m";
  static final String MONTHS = "m";
  static final String ONE_MONTH_AGO = "1" + MONTH;
  static final String IN_ONE_MONTH = "1" + MONTH;

  static final String YEAR = "y";
  static final String YEARS = "y";
  static final String ONE_YEAR_AGO = "1" + YEAR;
  static final String IN_ONE_YEAR = "1" + YEAR;

  static final ArrayMap<String, String> ONE_THING_AGO = new ArrayMap<>();
  static final ArrayMap<String, String> IN_ONE_THING = new ArrayMap<>();

  static {
    ONE_THING_AGO.put("m", ONE_MINUTE_AGO);
    ONE_THING_AGO.put("d", ONE_DAY_AGO);
    ONE_THING_AGO.put("h", ONE_HOUR_AGO);
    ONE_THING_AGO.put("w", ONE_WEEK_AGO);
    ONE_THING_AGO.put("mo", ONE_MONTH_AGO);
    ONE_THING_AGO.put("y", ONE_YEAR_AGO);
  }

  static {
    IN_ONE_THING.put("m", IN_ONE_MINUTE);
    IN_ONE_THING.put("d", IN_ONE_DAY);
    IN_ONE_THING.put("h", IN_ONE_HOUR);
    IN_ONE_THING.put("w", IN_ONE_WEEK);
    IN_ONE_THING.put("mo", IN_ONE_MONTH);
    IN_ONE_THING.put("y", IN_ONE_YEAR);
  }
}