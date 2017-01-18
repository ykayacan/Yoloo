package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.Key;
import com.yoloo.backend.validator.Rule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.yoloo.backend.OfyService.ofy;

public class NotFoundRule implements Rule<NotFoundException> {

  private static final Logger logger =
      Logger.getLogger(NotFoundRule.class.getName());

  private List<String> ids = new ArrayList<>(2);

  public NotFoundRule(String... ids) {
    this.ids.addAll(Arrays.asList(ids));
  }

  @Override
  public void validate() throws NotFoundException {
    for (String id : ids) {
      Key<?> entityKey = Key.create(id);

      try {
        ofy().load().kind(entityKey.getKind()).filter("__key__ =", entityKey)
            .keys().first().safe();
      } catch (com.googlecode.objectify.NotFoundException e) {
        throw new NotFoundException("Could not find " +
            entityKey.getKind().toLowerCase() + " with ID: " + id);
      }
    }
  }
}
