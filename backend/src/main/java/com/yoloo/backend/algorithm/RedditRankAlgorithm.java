package com.yoloo.backend.algorithm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class RedditRankAlgorithm {

    // TODO: 28.11.2016 Change rank algorithm.

    private static final DecimalFormat FORMAT = new DecimalFormat("####.#######",
            DecimalFormatSymbols.getInstance(Locale.US));

    public static RedditRankAlgorithm newInstance() {
        return new RedditRankAlgorithm();
    }

    private static double round(final double value) {
        return Double.parseDouble(FORMAT.format(value));
    }

    private static long score(long ups, long downs) {
        return ups - downs;
    }

    private static byte sign(final long score) {
        if (score > 0) {
            return 1;
        } else if (score < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public static double getHotRank(long ups, long downs, Date createdAt) {
        final long score = score(ups, downs);

        final double order = Math.log10(Math.max(Math.abs(score), 1));

        final byte sign = sign(score);

        final long seconds = (createdAt.getTime() / 1000) - 1134028003;

        final double rank = sign * order + seconds / 45000;

        return round(rank);
    }
}
