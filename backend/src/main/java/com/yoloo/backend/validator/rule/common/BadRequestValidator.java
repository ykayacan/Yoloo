package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.BadRequestException;
import com.google.common.collect.Lists;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.validator.Rule;
import java.util.List;

public class BadRequestValidator implements Rule<BadRequestException> {

  private final List<Object> objects;

  public BadRequestValidator(Object... objects) {
    this.objects = Lists.newArrayList(objects);
  }

  @Override
  public void validate() throws BadRequestException {
    for (Object o : this.objects) {
      Guard.checkBadRequest(o,
          (o instanceof String) ? o : o.getClass().getName() + " can not be empty.");
    }
  }
}
