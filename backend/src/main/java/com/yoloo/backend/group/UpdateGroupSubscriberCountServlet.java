package com.yoloo.backend.group;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateGroupSubscriberCountServlet extends HttpServlet {

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    updateGroupSubscribers();
  }

  private void updateGroupSubscribers() {
    List<Key<TravelerGroupEntity>> groupKeys =
        ofy().load().type(TravelerGroupEntity.class).keys().list();

    Map<Key<TravelerGroupEntity>, TravelerGroupEntity> groupMap = ofy().load().keys(groupKeys);

    for (Map.Entry<Key<TravelerGroupEntity>, TravelerGroupEntity> entry : groupMap.entrySet()) {
      int subscriberCount = getSubscriberCount(entry.getKey());
      entry.setValue(entry.getValue().withSubscriberCount(subscriberCount));
    }

    ofy().save().entities(groupMap.values());
  }

  private int getSubscriberCount(Key<TravelerGroupEntity> key) {
    return ofy().load()
        .type(Account.class)
        .filter(Account.FIELD_SUBSCRIBED_GROUP_KEYS, key)
        .keys()
        .list()
        .size();
  }
}
