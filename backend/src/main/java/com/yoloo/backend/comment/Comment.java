package com.yoloo.backend.comment;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
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
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment implements Votable {

    public static final String FIELD_QUESTION_KEY = "questionKey";
    public static final String FIELD_CREATED = "created";

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long id;

    @Parent
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> parentUser;

    @Index
    @NonFinal
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Question> questionKey;

    private String username;

    private Link avatarUrl;

    private String content;

    @Index(IfNotDefault.class)
    boolean accepted;

    @Index
    @NonFinal
    private DateTime created;

    // Extra fields

    @Ignore
    private Vote.Direction dir;

    @Ignore
    private long votes;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Comment> getKey() {
        return Key.create(parentUser.getKey(), getClass(), id);
    }

    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }

    @Override
    @SuppressWarnings("unchecked")
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public <T> Key<T> getVotableKey() {
        return (Key<T>) getKey();
    }
}