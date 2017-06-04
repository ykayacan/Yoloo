package com.yoloo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.country.Country;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentShard;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.travelertype.TravelerTypeEntity;
import com.yoloo.backend.relationship.Relationship;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagShard;
import com.yoloo.backend.group.TravelerGroupShard;
import com.yoloo.backend.vote.Vote;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public final class OfyService {

  static {
    JodaTimeTranslators.add(factory());

    factory().register(Account.class);
    factory().register(AccountShard.class);

    factory().register(Feed.class);
    factory().register(PostEntity.class);
    factory().register(PostEntity.PostShard.class);

    factory().register(Tag.class);
    factory().register(TagShard.class);

    factory().register(TravelerTypeEntity.class);
    factory().register(TravelerGroupEntity.class);
    factory().register(TravelerGroupShard.class);

    factory().register(Comment.class);
    factory().register(CommentShard.class);

    factory().register(Vote.class);

    factory().register(Tracker.class);
    factory().register(Relationship.class);

    factory().register(Bookmark.class);

    factory().register(DeviceRecord.class);

    factory().register(Notification.class);
    factory().register(MediaEntity.class);

    factory().register(Country.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
