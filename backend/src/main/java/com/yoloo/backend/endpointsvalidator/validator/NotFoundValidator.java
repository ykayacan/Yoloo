package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.Key;
import com.yoloo.backend.endpointsvalidator.Validator;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class NotFoundValidator implements Validator {

  private String itemId;
  private String message;

  @Override
  public boolean isValid() {
    try {
      ofy().load().key(Key.create(itemId)).safe();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public void onException() throws ServiceException {
    throw new NotFoundException(message);
  }
}
