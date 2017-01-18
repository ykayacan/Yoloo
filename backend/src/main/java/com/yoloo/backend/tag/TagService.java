package com.yoloo.backend.tag;

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import io.reactivex.Single;
import java.util.List;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.factory;

@NoArgsConstructor(staticName = "create")
public class TagService {

  public Single<Tag> createTag(String name, String language, String groupIds) {
    final Key<Tag> key = factory().allocateId(Tag.class);

    List<Key<TagGroup>> groupKeys = TagUtil.extractGroupKeys(groupIds).blockingGet();

    return Single.just(Tag.builder()
        .id(key.getId())
        .name(name)
        .language(language)
        .groupKeys(groupKeys)
        .build());
  }

  public Single<Tag> updateTag(Tag tag, Optional<String> name) {
    if (name.isPresent()) {
      tag = tag.withName(name.get());
    }

    return Single.just(tag);
  }

  public Single<TagGroup> createGroup(String name) {
    return Single.just(TagGroup.builder()
        .name(name)
        .totalTagCount(0)
        .totalQuestionCount(0)
        .build());
  }

  public Single<TagGroup> updateGroup(TagGroup group, Optional<String> name) {
    if (name.isPresent()) {
      group = group.withName(name.get());
    }

    return Single.just(group);
  }
}
