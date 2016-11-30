package com.yoloo.backend.account;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.badge.Badge;

import java.util.Date;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountDetail {

    @Id
    private long id;

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Account> parentUserKey;

    private String realName;

    private String locale;

    private Gender gender;

    private Date birthDate;

    private Set<Badge> badges;

    public boolean isBadgeTaken(Badge badge) {
        Preconditions.checkNotNull(badge, "badge can not be null.");
        return badges.contains(badge);
    }

    public enum Gender {
        /**
         * Male gender.
         */
        MALE,
        /**
         * Female gender.
         */
        FEMALE,
        /**
         * Unspecified gender.
         */
        UNSPECIFIED
    }
}
