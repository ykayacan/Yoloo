package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;
import com.yoloo.backend.endpointsvalidator.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class BadRequestValidator implements Validator {
  private Object object;
  private String message;

  @Override public boolean valid() {
    return object != null;
  }

  @Override public void onException() throws ServiceException {
    throw new BadRequestException(message);
  }
}
