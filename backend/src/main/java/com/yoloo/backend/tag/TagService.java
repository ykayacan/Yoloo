package com.yoloo.backend.tag;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import ix.Ix;
import java.util.Collection;
import java.util.List;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
public class TagService {

  public List<Tag> updateTags(Collection<String> tagNames) {
    List<Tag> persistentTags = Ix
        .from(tagNames)
        .map(Tag::createKey)
        .collectToList()
        .flatMap(keys -> Ix.from(ofy().load().keys(keys).values()))
        .map(tag -> tag.withPostCount(tag.getPostCount() + 1))
        .toList();

    List<String> persistentTagsAsNames = Ix.from(persistentTags).map(Tag::getName).toList();

    Collection<String> newTagNames =
        Collections2.filter(tagNames, Predicates.not(Predicates.in(persistentTagsAsNames)));

    if (!newTagNames.isEmpty()) {
      List<Tag> newTags = Ix
          .from(newTagNames)
          .map(tagName -> Tag
              .builder()
              .id(Tag.createKey(tagName).getName())
              .name(tagName)
              .rank(0.0D)
              .postCount(1L)
              .build())
          .toList();

      persistentTags.addAll(newTags);
    }

    return persistentTags;
  }
}
