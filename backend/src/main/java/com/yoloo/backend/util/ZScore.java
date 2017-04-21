package com.yoloo.backend.util;

public final class ZScore {
  // The rate at which the historic data's effect will diminish
  private double decay;

  private double avg;
  private double sqrAvg;

  public ZScore(double decay, int[] pop) {
    this.sqrAvg = 0;
    this.avg = 0;
    this.decay = decay;

    for (int aPop : pop) {
      update(aPop);
    }
  }

  private void update(int value) {
    if (avg == 0 && sqrAvg == 0) {
      // Set initial averages to the first value in the sequence.
      avg = (double) value;
      sqrAvg = Math.pow(value, 2);
    } else {
      // Calculate the average of the rest of the values using a floating average.
      avg = avg * decay + value * (1 - decay);
      sqrAvg = sqrAvg * decay + Math.pow(value, 2) * (1 - decay);
    }
  }

  private double standard() {
    // Somewhat ad-hoc standard deviation calculation.
    return Math.sqrt(sqrAvg - Math.pow(avg, 2));
  }

  public double getScore(int obs) {
    final double  standard = standard();
    if (standard == 0) {
      return (obs - avg);
    } else {
      return (obs - avg) / standard;
    }
  }
}
