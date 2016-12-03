package com.yoloo.backend.question;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
public class Feed {

    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    @NonFinal
    @Load
    private Ref<Question> questionRef;

    public Question getQuestion() {
        return questionRef.get();
    }
}
