package com.yoloo.backend.tag;

import com.google.common.base.Optional;

import com.googlecode.objectify.Key;

import java.util.List;

import io.reactivex.Single;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.factory;

@AllArgsConstructor(staticName = "create")
public class TagService {

    public Single<Tag> createTag(String name, String language, List<Key<TagGroup>> groupKeys) {
        final Key<Tag> key = factory().allocateId(Tag.class);

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
