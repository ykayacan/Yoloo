package com.yoloo.backend.category.rank;

import com.yoloo.backend.algorithm.RankAlgorithm;

import java.util.Date;

import lombok.AllArgsConstructor;

/**
 * See http://julesjacobs.github.io/2015/05/06/exponentially-decaying-likes.html
 */
@AllArgsConstructor(staticName = "from")
public class CategoryRankAlgorithm implements RankAlgorithm {

    private long questionCount;

    /**
     * Number of milliseconds in one hour.
     */
    private static final double RATE = 1/3.6e6;

    /**
     *
     * @param z previous question count of the category.
     * @param time
     * @return rank.
     */
    private static double updateRank(long z, long time) {
        double u = Math.max(z, RATE * time);
        double v = Math.min(z, RATE * time);
        return u + Math.log1p(Math.exp(v - u));
    }

    @Override
    public double getRank() {
        return updateRank(questionCount, new Date().getTime());
    }
}