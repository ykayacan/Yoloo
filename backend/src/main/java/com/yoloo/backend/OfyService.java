package com.yoloo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentCounterShard;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagCounterShard;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.topic.TopicCounterShard;
import com.yoloo.backend.vote.Vote;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public final class OfyService {

  static {
    JodaTimeTranslators.add(factory());

    factory().register(Account.class);
    factory().register(AccountCounterShard.class);

    factory().register(Feed.class);
    factory().register(Question.class);
    factory().register(QuestionCounterShard.class);

    factory().register(Tag.class);
    factory().register(TagCounterShard.class);

    factory().register(Topic.class);
    factory().register(TopicCounterShard.class);

    factory().register(Comment.class);
    factory().register(CommentCounterShard.class);

    factory().register(Vote.class);

    factory().register(Tracker.class);
    factory().register(Follow.class);

    factory().register(Bookmark.class);

    factory().register(DeviceRecord.class);

    factory().register(Notification.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
