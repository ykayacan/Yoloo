package com.yoloo.backend.tag;

import com.google.common.base.Optional;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.factory;

@NoArgsConstructor(staticName = "create")
public class TagService {

  public Single<Tag> createTag(String name, String language, String groupIds) {
    return TagUtil.extractGroupKeys(groupIds)
        .toList()
        .flatMap(groupKeys -> Single.just(
            Tag.builder()
                .id(factory().allocateId(Tag.class).getId())
                .name(name)
                .language(language)
                .type(Tag.Type.NORMAL)
                .groupKeys(groupKeys)
                .build()));
  }

  public Single<Tag> updateTag(Tag tag, Optional<String> name) {
    return Single.just(name)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(s -> Maybe.just(tag.withName(s)))
        .toSingle();
  }

  public Single<Tag> createGroup(String name) {
    return Single.just(
        Tag.builder()
            .id(factory().allocateId(Tag.class).getId())
            .name(name)
            .type(Tag.Type.GROUP)
            .totalTagCount(0)
            .questions(0)
            .build());
  }

  public Single<Tag> updateGroup(Tag group, Optional<String> name) {
    return Single.just(name)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(s -> Maybe.just(group.withName(s)))
        .toSingle();
  }
}
