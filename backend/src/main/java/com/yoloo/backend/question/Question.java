package com.yoloo.backend.question;

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
import com.yoloo.backend.account.Account;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.vote.Votable;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

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
public class Question implements Votable {

    public static final String FIELD_CREATED = "created";
    public static final String FIELD_HASHTAGS = "hashTags";
    public static final String FIELD_FIRST_COMMENT = "firstComment";
    public static final String FIELD_RANK = "rank";
    public static final String FIELD_BOUNTY = "bounty";

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long id;

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Account> parentUserKey;

    private Link avatarUrl;

    private String username;

    private String content;

    @Index
    @NonFinal
    private DateTime created;

    @Index
    @NonFinal
    private Set<String> hashTags;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Set<Key<Account>> reportedByKeys;

    private Set<String> categories;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Set<Key<Category>> categoryKeys;

    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Comment> acceptedComment;

    /**
     * The bounty value for the question.
     * Bounty list is given below.
     * 10, 20, 30, 40, 50
     */
    @Index
    private int bounty;

    /**
     * If a user questions a comment for given post then commented is true otherwise false.
     */
    @Index
    @NonFinal
    private boolean firstComment;

    @Index
    @NonFinal
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private double rank;

    private Media media;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private List<Key<QuestionCounterShard>> shardKeys;

    // Extra fields

    @Ignore
    private Vote.Direction dir;

    @Ignore
    private long votes;

    @Ignore
    @Min(0)
    private long comments;

    @Ignore
    @Size(max = 3)
    private int reports;

    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Question> getKey() {
        return Key.create(parentUserKey, getClass(), id);
    }

    public String getAcceptedCommentId() {
        return acceptedComment.getKey().toWebSafeString();
    }

    @Override
    @SuppressWarnings("unchecked")
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public <T> Key<T> getVotableKey() {
        return (Key<T>) getKey();
    }
}
