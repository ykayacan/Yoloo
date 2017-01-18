package com.yoloo.backend.validator.rule.question;

import com.google.api.server.spi.response.BadRequestException;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.validator.Rule;

public class QuestionCreateRule implements Rule<BadRequestException> {

  private final String content;
  private final String tags;

  public QuestionCreateRule(String content, String tags) {
    this.content = content;
    this.tags = tags;
  }

  @Override
  public void validate() throws BadRequestException {
    Guard.checkBadRequest(this.tags, "Tag can not be empty.");
    Guard.checkBadRequest(this.content, "Content can not be empty.");
  }
}
