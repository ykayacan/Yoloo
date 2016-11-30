package com.yoloo.backend.saved;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;

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
public class SavedQuestion {

    public static final String FIELD_CREATED = "created";

    /**
     * websafe question id.
     */
    @Id
    private String id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private DateTime created;

    public Key<Question> getSavedQuestionKey() {
        return Key.create(id);
    }
}
