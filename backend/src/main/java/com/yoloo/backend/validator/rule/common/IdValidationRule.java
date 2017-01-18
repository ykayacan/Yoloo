package com.yoloo.backend.validator.rule.common;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;
import com.yoloo.backend.validator.Rule;

public class IdValidationRule implements Rule<BadRequestException> {

  private final String id;

  public IdValidationRule(final String id) {
    this.id = id;
  }

  @Override
  public void validate() throws BadRequestException {
    if (Strings.isNullOrEmpty(this.id)) {
      throw new BadRequestException(
          "Given item id " + this.id + " is invalid.");
    }
  }
}
