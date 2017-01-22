package com.yoloo.android.feature.ui.widget.zamanview;

import java.util.HashMap;

class ZamanTimeString {
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

  static final HashMap<String, String> ONE_THING_AGO = new HashMap<>();
  static final HashMap<String, String> IN_ONE_THING = new HashMap<>();

  static {
    ONE_THING_AGO.put("minute", ONE_MINUTE_AGO);
    ONE_THING_AGO.put("day", ONE_DAY_AGO);
    ONE_THING_AGO.put("hour", ONE_HOUR_AGO);
    ONE_THING_AGO.put("week", ONE_WEEK_AGO);
    ONE_THING_AGO.put("month", ONE_MONTH_AGO);
    ONE_THING_AGO.put("year", ONE_YEAR_AGO);
  }

  static {
    IN_ONE_THING.put("minute", IN_ONE_MINUTE);
    IN_ONE_THING.put("day", IN_ONE_DAY);
    IN_ONE_THING.put("hour", IN_ONE_HOUR);
    IN_ONE_THING.put("week", IN_ONE_WEEK);
    IN_ONE_THING.put("month", IN_ONE_MONTH);
    IN_ONE_THING.put("year", IN_ONE_YEAR);
  }
}