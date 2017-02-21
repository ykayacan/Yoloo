package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.endpointsvalidator.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class CommentUpdateValidator implements Validator {

  private String postId;
  private String commentId;
  private User user;

  @Override public boolean valid() {
    try {
      final Key<?> postKey = Key.create(postId);
      final Key<?> commentKey = Key.create(commentId);
      final Key<Account> userKey = Key.create(user.getUserId());

      return commentKey.<Account>getParent().equivalent(userKey)
          || postKey.<Account>getParent().equivalent(userKey);
    } catch (Exception e) {
      return false;
    }
  }

  @Override public void onException() throws ServiceException {
    throw new ForbiddenException("Not has permission to update.");
  }
}
