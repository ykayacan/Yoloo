package com.yoloo.backend.endpointsvalidator;

import com.google.api.server.spi.ServiceException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class EndpointsValidator {

  public EndpointsValidator on(Validator validator) throws ServiceException {
    if (!validator.isValid()) {
      validator.onException();
    }
    return this;
  }
}
