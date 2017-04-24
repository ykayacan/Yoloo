package com.yoloo.backend.tag;

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

    return Ix
        .from(tagNames)
        .filter(s -> !persistentTagsAsNames.contains(s))
        .collectToList()
        .filter(strings -> !strings.isEmpty())
        .flatMap(Ix::from)
        .map(tagName -> Tag
            .builder()
            .id(Tag.createKey(tagName).getName())
            .name(tagName)
            .rank(0.0D)
            .postCount(1L)
            .build())
        .mergeWith(persistentTags)
        .toList();
  }
}
