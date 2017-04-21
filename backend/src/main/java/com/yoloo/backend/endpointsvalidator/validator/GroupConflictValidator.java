package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ConflictException;
import com.yoloo.backend.endpointsvalidator.Validator;
import com.yoloo.backend.travelertype.TravelerTypeEntity;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class GroupConflictValidator implements Validator {

  private String displayName;

  @Override
  public boolean isValid() {
    return ofy().load().key(TravelerTypeEntity.createKey(displayName)).now() == null;
  }

  @Override
  public void onException() throws ServiceException {
    throw new ConflictException("group exists.");
  }
}
