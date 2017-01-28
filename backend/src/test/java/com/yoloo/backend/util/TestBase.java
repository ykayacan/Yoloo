package com.yoloo.backend.util;

import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.googlecode.objectify.util.Closeable;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.blog.Blog;
import com.yoloo.backend.blog.BlogCounterShard;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentCounterShard;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagCounterShard;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.topic.TopicCounterShard;
import com.yoloo.backend.vote.Vote;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;

@RunWith(JUnit4.class)
public class TestBase extends GAETestBase {

  private Closeable rootService;

  @Before
  public void setUp() {
    this.setUpObjectifyFactory(new TestObjectifyFactory());
    JodaTimeTranslators.add(fact());

    fact().register(Account.class);
    fact().register(AccountCounterShard.class);

    fact().register(Question.class);
    fact().register(QuestionCounterShard.class);

    fact().register(Blog.class);
    fact().register(BlogCounterShard.class);

    fact().register(Tag.class);
    fact().register(TagCounterShard.class);

    fact().register(Topic.class);
    fact().register(TopicCounterShard.class);

    fact().register(Comment.class);
    fact().register(CommentCounterShard.class);

    fact().register(Follow.class);
    fact().register(Vote.class);
    fact().register(Feed.class);
    fact().register(Tracker.class);
    fact().register(DeviceRecord.class);
    fact().register(Notification.class);
    fact().register(Media.class);
    fact().register(Bookmark.class);
  }

  @After
  public void tearDown() {
    rootService.close();
    rootService = null;
  }

  protected void setUpObjectifyFactory(TestObjectifyFactory factory) {
    if (rootService != null) {
      rootService.close();
    }

    TestObjectifyService.setFactory(factory);
    rootService = TestObjectifyService.begin();
  }
}
