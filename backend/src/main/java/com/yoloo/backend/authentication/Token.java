package com.yoloo.backend.authentication;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.account.Account;

import java.util.Date;

import lombok.Builder;
import lombok.Value;

@Entity
@Cache
@Value
@Builder(toBuilder = true)
public class Token {

    @Id
    private Long id;

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Account> userKey;

    @Index
    private String accessToken;

    @Index
    private String refreshToken;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Date created;

    // Extra fields

    @Ignore
    private String tokenType = OAuth2.OAUTH_HEADER_NAME;

    @Ignore
    private long expiresIn = Constants.TOKEN_EXPIRES_IN;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Token> getKey() {
        return Key.create(userKey, Token.class, id);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isTokenExpired() {
        long expireInSec = Constants.TOKEN_EXPIRES_IN * 1000;
        long currentTime = System.currentTimeMillis();
        return expireInSec + created.getTime() < currentTime;
    }
}
