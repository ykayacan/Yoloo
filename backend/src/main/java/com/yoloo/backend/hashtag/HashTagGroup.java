package com.yoloo.backend.hashtag;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HashTagGroup {

    @Id
    private Long id;

    private String name;

    private long usageCount;
}
