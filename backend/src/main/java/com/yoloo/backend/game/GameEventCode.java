package com.yoloo.backend.game;

public final class GameEventCode {

  public static final class Point {
    public static final int FIRST_QUESTION = 0;
    public static final int FIRST_ANSWER = 1;
    public static final int DAILY_ASK_QUESTION = 2;
    public static final int DAILY_FIRST_ANSWERER = 3;
    public static final int ANSWER_TO_UNANSWERED = 4;
    public static final int ACCEPT_COMMENT = 5;
    public static final int SHARE_POST = 6;
    public static final int INVITE_FRIENS = 7;
    public static final int RATE_APP = 8;
  }

  public static final class Bounty {
    public static final int FIRST_QUESTION = 50;
    public static final int FIRST_ANSWER = 51;
    public static final int DAILY_ASK_QUESTION = 52;
    public static final int ACCEPTED_COMMENT = 53;
    public static final int INVITE_FRIENS = 54;
    public static final int RATE_APP = 55;
  }
}
