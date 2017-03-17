package com.yoloo.backend.util;

import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.googlecode.objectify.util.Closeable;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.CategoryShard;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentShard;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagShard;
import com.yoloo.backend.vote.Vote;
import org.junit.After;
import org.junit.Before;

import static com.yoloo.backend.util.TestObjectifyService.fact;

public class TestBase extends GAETestBase {

  private Closeable rootService;

  @Before public void setUp() {
    this.setUpObjectifyFactory(new TestObjectifyFactory());
    JodaTimeTranslators.add(fact());

    fact().register(Account.class);
    fact().register(AccountShard.class);

    fact().register(Post.class);
    fact().register(PostShard.class);

    fact().register(Tag.class);
    fact().register(TagShard.class);

    fact().register(Category.class);
    fact().register(CategoryShard.class);

    fact().register(Comment.class);
    fact().register(CommentShard.class);

    fact().register(Follow.class);
    fact().register(Vote.class);
    fact().register(Feed.class);
    fact().register(Tracker.class);
    fact().register(DeviceRecord.class);
    fact().register(Notification.class);
    fact().register(Media.class);
    fact().register(Bookmark.class);
  }

  @After public void tearDown() {
    rootService.close();
    rootService = null;
  }

  private void setUpObjectifyFactory(TestObjectifyFactory factory) {
    if (rootService != null) {
      rootService.close();
    }

    TestObjectifyService.setFactory(factory);
    rootService = TestObjectifyService.begin();
  }
}
