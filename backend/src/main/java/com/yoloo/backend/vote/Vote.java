package com.yoloo.backend.vote;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Vote {

    public static final String FIELD_VOTABLE_KEY = "votableKey";

    // Websafe voteable id.
    @Id
    private String id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    @NonFinal
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<? extends Votable> votableKey;

    private Direction dir;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Vote> getKey() {
        return Key.create(parentUserKey, Vote.class, id);
    }

    @AllArgsConstructor
    @Getter
    public enum Direction {
        DEFAULT(0), UP(1), DOWN(-1);

        private int value;
    }
}