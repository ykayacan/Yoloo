package com.yoloo.backend.game;

import com.yoloo.backend.account.Account;
import ix.Ix;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

/**
 * Resets daily points each day.
 */
public class ResetDailyPointsServlet extends HttpServlet {

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    List<Tracker> updatedTrackers = Ix.from(ofy().load().type(Account.class).keys().list())
        .map(Tracker::createKey)
        .collectToList()
        .map(keys -> ofy().load().keys(keys).values())
        .flatMap(Ix::from)
        .map(tracker -> {
          tracker.setDailyPoints(0);
          return tracker;
        })
        .toList();

    ofy().save().entities(updatedTrackers);
  }
}
