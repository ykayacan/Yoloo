package com.yoloo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountDetail;
import com.yoloo.backend.authentication.Token;
import com.yoloo.backend.comment.CommentCounterShard;
import com.yoloo.backend.fcm.RegistrationRecord;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.hashtag.HashTag;
import com.yoloo.backend.vote.Vote;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OfyService {

    static {
        JodaTimeTranslators.add(factory());

        factory().register(Account.class);
        factory().register(AccountDetail.class);
        factory().register(AccountCounterShard.class);

        factory().register(Token.class);

        factory().register(Question.class);
        factory().register(QuestionCounterShard.class);

        factory().register(HashTag.class);

        //factory().register(Comment.class);
        factory().register(CommentCounterShard.class);

        factory().register(Vote.class);

        factory().register(RegistrationRecord.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
