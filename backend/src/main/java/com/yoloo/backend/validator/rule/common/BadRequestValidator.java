package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.BadRequestException;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.validator.Rule;
import java.util.Arrays;
import java.util.List;

public class BadRequestValidator implements Rule<BadRequestException> {

  private final List<Object> objects;

  public BadRequestValidator(Object... objects) {
    this.objects = Arrays.asList(objects);
  }

  @Override
  public void validate() throws BadRequestException {
    for (Object o : objects) {
      Guard.checkBadRequest(o, "parameters can not be empty.");
    }
  }
}
