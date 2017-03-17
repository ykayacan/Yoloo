package com.yoloo.backend.endpointsvalidator;

import com.google.api.server.spi.ServiceException;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Singular;

@NoArgsConstructor(staticName = "create")
public class EndpointsValidator {

  @Singular(value = "on")
  private List<Validator> validators = new ArrayList<>(5);

  public EndpointsValidator on(Validator validator) {
    validators.add(validator);
    return this;
  }

  public void validate() throws ServiceException {
    for (Validator validator : validators) {
      if (!validator.valid()) {
        validator.onException();
        break;
      }
    }
  }
}
