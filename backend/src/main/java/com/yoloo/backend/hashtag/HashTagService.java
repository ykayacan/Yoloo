package com.yoloo.backend.hashtag;

import com.google.common.collect.ImmutableSet;

import com.yoloo.backend.util.StringUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
public class HashTagService {

    public ImmutableSet<HashTag> createHashTags(String hashTags) {
        ImmutableSet<String> bareTags = StringUtil.splitToSet(hashTags, ",");

        ImmutableSet.Builder<HashTag> hashTagBuilder = ImmutableSet.builder();

        for (String hashTag : bareTags) {
            HashTag tag = HashTag.builder()
                    .groupIds(findGroups(hashTag))
                    .name(hashTag)
                    .build();

            hashTagBuilder.add(tag);
        }

        return hashTagBuilder.build();
    }

    public ImmutableSet<Integer> findGroups(String tag) {
        // TODO: 26.11.2016 Implement real hashtag group finder.
        return ImmutableSet.of();
    }
}
