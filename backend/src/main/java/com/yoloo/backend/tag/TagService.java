package com.yoloo.backend.tag;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.googlecode.objectify.cmd.Query;
import ix.Ix;
import java.util.Collection;
import java.util.List;

import static com.yoloo.backend.OfyService.ofy;

public class TagService {

  public List<Tag> updateTags(Collection<String> tagNames) {
    List<Tag> availableTags = Ix.from(findUsedTagsFromNames(tagNames))
        .map(tag -> tag.withPostCount(tag.getPostCount() + 1))
        .toList();

    List<String> availableTagNames = Ix.from(availableTags).map(Tag::getName).toList();

    Collection<String> newTagNames =
        Collections2.filter(tagNames, Predicates.not(Predicates.in(availableTagNames)));

    List<Tag> newTags = Ix.from(newTagNames).map(tagName -> Tag.builder()
        .name(tagName)
        .rank(0.0D)
        .postCount(1)
        .build())
        .toList();

    availableTags.addAll(newTags);

    return availableTags;
  }

  private List<Tag> findUsedTagsFromNames(Collection<String> tagNames) {
    Query<Tag> query = ofy().load().type(Tag.class);
    for (String tagName : tagNames) {
      query = query.filter(tagName + "=", Tag.FIELD_NAME);
    }

    return query.list();
  }
}
