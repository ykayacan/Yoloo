package com.yoloo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentShard;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagShard;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.CategoryShard;
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
    factory().register(Post.class);
    factory().register(PostShard.class);

    factory().register(Tag.class);
    factory().register(TagShard.class);

    factory().register(Category.class);
    factory().register(CategoryShard.class);

    factory().register(Comment.class);
    factory().register(CommentShard.class);

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
