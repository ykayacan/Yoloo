package com.yoloo.backend.account;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.yoloo.backend.algorithm.bcrypt.BCrypt;

import org.joda.time.DateTime;

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
public class Account {

    public static final String FIELD_EMAIL = "email";

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long id;

    @Index
    private String username;

    @Index
    private Email email;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String password;

    @Index
    private Provider provider;

    private Link avatarUrl;

    @Load
    private Ref<AccountDetail> detail;

    @Index
    private DateTime created;

    // Extra fields

    @Ignore
    private long followings;

    @Ignore
    private long followers;

    @Ignore
    private long questions;

    @Ignore
    private boolean isFollowing;

    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Account> getKey() {
        return Key.create(Account.class, id);
    }

    public boolean isValidPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public enum Provider {
        /**
         * Google provider.
         */
        GOOGLE(1),
        /**
         * Facebook provider.
         */
        FACEBOOK(2),
        /**
         * Yoloo provider.
         */
        YOLOO(3);

        private final int provider;

        Provider(int provider) {
            this.provider = provider;
        }
    }
}
