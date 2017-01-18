package com.yoloo.backend.question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionWrapper {

  private String questionId;
  private String content;
  private String tags;
  private String mediaId;
  private String topics;
  private int bounty;
  private String date;
}
