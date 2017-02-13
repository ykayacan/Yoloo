/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yoloo.android.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import com.google.common.annotations.VisibleForTesting;
import com.yoloo.android.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Collection of date utilities.
 */
public final class Dates {
  public static final long SECOND_IN_MILLIS = 1000;
  public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
  public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
  public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
  public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
  // Flags to specify whether or not to use 12 or 24 hour mode.
  // Callers of methods in this class should never have to specify these; this is really
  // intended only for unit tests.
  @SuppressWarnings("deprecation")
  @VisibleForTesting public static final int FORCE_12_HOUR = DateUtils.FORMAT_12HOUR;
  @SuppressWarnings("deprecation")
  @VisibleForTesting public static final int FORCE_24_HOUR = DateUtils.FORMAT_24HOUR;

  /**
   * Private default constructor
   */
  private Dates() {
  }

  /**
   * Get the relative time as a string
   *
   * @param time The time
   * @return The relative time
   */
  public static CharSequence getRelativeTimeSpanString(Context context, final long time) {
    final long now = System.currentTimeMillis();
    if (now - time < DateUtils.MINUTE_IN_MILLIS) {
      // Also fixes bug where posts appear in the future
      return context.getApplicationContext().getResources().getText(R.string.posted_just_now);
    }

    /*
    * Workaround for b/5657035. The platform method {@link DateUtils#getRelativeTimeSpan()}
    * passes a null context to other platform methods. However, on some devices, this
    * context is dereferenced when it shouldn't be and an NPE is thrown. We catch that
    * here and use a slightly less precise time.
    */
    try {
      return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS,
          DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    } catch (final NullPointerException npe) {
      return getShortRelativeTimeSpanString(context.getApplicationContext(), time);
    }
  }

  public static CharSequence getConversationTimeString(Context context, final long time) {
    return getTimeString(context, time, true /*abbreviated*/, false /*minPeriodToday*/);
  }

  public static CharSequence getMessageTimeString(Context context, final long time) {
    return getTimeString(context, time, false /*abbreviated*/, false /*minPeriodToday*/);
  }

  public static CharSequence getWidgetTimeString(Context context, final long time,
      final boolean abbreviated) {
    return getTimeString(context, time, abbreviated, true /*minPeriodToday*/);
  }

  public static CharSequence getFastScrollPreviewTimeString(Context context, final long time) {
    return getTimeString(context, time, true /* abbreviated */, true /* minPeriodToday */);
  }

  public static CharSequence getMessageDetailsTimeString(Context context, final long time) {
    int flags;
    if (DateFormat.is24HourFormat(context)) {
      flags = FORCE_24_HOUR;
    } else {
      flags = FORCE_12_HOUR;
    }
    return getOlderThanAYearTimestamp(context, time, LocaleUtil.getCurrentLocale(context),
        false /*abbreviated*/, flags);
  }

  private static CharSequence getTimeString(Context context, final long time,
      final boolean abbreviated, final boolean minPeriodToday) {
    int flags;
    if (DateFormat.is24HourFormat(context)) {
      flags = FORCE_24_HOUR;
    } else {
      flags = FORCE_12_HOUR;
    }
    return getTimestamp(context, time, System.currentTimeMillis(), abbreviated,
        LocaleUtil.getCurrentLocale(context), flags, minPeriodToday);
  }

  @VisibleForTesting
  public static CharSequence getTimestamp(Context context, final long time, final long now,
      final boolean abbreviated, final Locale locale, final int flags,
      final boolean minPeriodToday) {
    final long timeDiff = now - time;
    if (!minPeriodToday && timeDiff < DateUtils.MINUTE_IN_MILLIS) {
      return getLessThanAMinuteOldTimeString(context.getApplicationContext(), abbreviated);
    } else if (!minPeriodToday && timeDiff < DateUtils.HOUR_IN_MILLIS) {
      return getLessThanAnHourOldTimeString(context.getApplicationContext(), timeDiff, flags);
    } else if (getNumberOfDaysPassed(time, now) == 0) {
      return getTodayTimeStamp(context.getApplicationContext(), time, flags);
    } else if (timeDiff < DateUtils.WEEK_IN_MILLIS) {
      return getThisWeekTimestamp(context.getApplicationContext(), time, locale, abbreviated,
          flags);
    } else if (timeDiff < DateUtils.YEAR_IN_MILLIS) {
      return getThisYearTimestamp(context.getApplicationContext(), time, locale, abbreviated,
          flags);
    } else {
      return getOlderThanAYearTimestamp(context.getApplicationContext(), time, locale, abbreviated,
          flags);
    }
  }

  private static CharSequence getLessThanAMinuteOldTimeString(
      Context context, final boolean abbreviated) {
    return context.getResources().getText(
        abbreviated ? R.string.posted_just_now : R.string.posted_now);
  }

  private static CharSequence getLessThanAnHourOldTimeString(Context context, final long timeDiff,
      final int flags) {
    final long count = timeDiff / MINUTE_IN_MILLIS;
    final String format = context.getResources().getQuantityString(
        R.plurals.num_minutes_ago, (int) count);
    return String.format(format, count);
  }

  private static CharSequence getTodayTimeStamp(Context context, final long time, final int flags) {
    return DateUtils.formatDateTime(context, time,
        DateUtils.FORMAT_SHOW_TIME | flags);
  }

  private static CharSequence getExplicitFormattedTime(final long time, final int flags,
      final String format24, final String format12) {
    SimpleDateFormat formatter;
    if ((flags & FORCE_24_HOUR) == FORCE_24_HOUR) {
      formatter = new SimpleDateFormat(format24, Locale.getDefault());
    } else {
      formatter = new SimpleDateFormat(format12, Locale.getDefault());
    }
    return formatter.format(new Date(time));
  }

  private static CharSequence getThisWeekTimestamp(final Context context, final long time,
      final Locale locale, final boolean abbreviated, final int flags) {
    if (abbreviated) {
      return DateUtils.formatDateTime(context, time,
          DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY | flags);
    } else {
      if (locale.equals(Locale.US)) {
        return getExplicitFormattedTime(time, flags, "EEE HH:mm", "EEE h:mmaa");
      } else {
        return DateUtils.formatDateTime(context, time,
            DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_ABBREV_WEEKDAY
                | flags);
      }
    }
  }

  private static CharSequence getThisYearTimestamp(Context context, final long time,
      final Locale locale, final boolean abbreviated, final int flags) {
    if (abbreviated) {
      return DateUtils.formatDateTime(context, time,
          DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH
              | DateUtils.FORMAT_NO_YEAR | flags);
    } else {
      if (locale.equals(Locale.US)) {
        return getExplicitFormattedTime(time, flags, "MMM d, HH:mm", "MMM d, h:mmaa");
      } else {
        return DateUtils.formatDateTime(context, time,
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_NO_YEAR
                | flags);
      }
    }
  }

  private static CharSequence getOlderThanAYearTimestamp(Context context, final long time,
      final Locale locale, final boolean abbreviated, final int flags) {
    if (abbreviated) {
      if (locale.equals(Locale.US)) {
        return getExplicitFormattedTime(time, flags, "M/d/yy", "M/d/yy");
      } else {
        return DateUtils.formatDateTime(context, time,
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_NUMERIC_DATE);
      }
    } else {
      if (locale.equals(Locale.US)) {
        return getExplicitFormattedTime(time, flags, "M/d/yy, HH:mm", "M/d/yy, h:mmaa");
      } else {
        return DateUtils.formatDateTime(context, time,
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR
                | flags);
      }
    }
  }

  public static CharSequence getShortRelativeTimeSpanString(Context context, final long time) {
    final long now = System.currentTimeMillis();
    final long duration = Math.abs(now - time);
    int resId;
    long count;
    if (duration < HOUR_IN_MILLIS) {
      count = duration / MINUTE_IN_MILLIS;
      resId = R.plurals.num_minutes_ago;
    } else if (duration < DAY_IN_MILLIS) {
      count = duration / HOUR_IN_MILLIS;
      resId = R.plurals.num_hours_ago;
    } else if (duration < WEEK_IN_MILLIS) {
      count = getNumberOfDaysPassed(time, now);
      resId = R.plurals.num_days_ago;
    } else {
      // Although we won't be showing a time, there is a bug on some devices that use
      // the passed in context. On these devices, passing in a {@code null} context
      // here will generateAll an NPE. See b/5657035.
      return DateUtils.formatDateRange(context, time, time,
          DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_RELATIVE);
    }
    final String format = context.getResources().getQuantityString(resId, (int) count);
    return String.format(format, count);
  }

  private static synchronized long getNumberOfDaysPassed(final long date1, final long date2) {
    return Days.daysBetween(new DateTime(date1), new DateTime(date2)).getDays();
  }
}
