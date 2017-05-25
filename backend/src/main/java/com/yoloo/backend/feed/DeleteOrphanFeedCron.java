package com.yoloo.backend.feed;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.yoloo.backend.post.PostEntity;
import ix.Ix;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class DeleteOrphanFeedCron extends HttpServlet {

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    process();
  }

  private void process() {
    Map<Key<Feed>, Key<PostEntity>> map = Ix.from(ofy().load().type(Feed.class).keys().list())
        .toMap(feedKey -> feedKey, Feed::getPostKey);

    ImmutableSet<Key<PostEntity>> availablePostKeys =
        ImmutableSet.copyOf(ofy().load().type(PostEntity.class).keys().list());

    // Collect only orphan feed keys.
    List<Key<Feed>> orphanFeedKeys = Stream.of(map)
        .dropWhile(value -> availablePostKeys.contains(value.getValue()))
        .map(Map.Entry::getKey)
        .toList();

    ofy().delete().keys(orphanFeedKeys);
  }
}
