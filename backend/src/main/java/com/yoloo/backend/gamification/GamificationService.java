package com.yoloo.backend.gamification;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.util.Group;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class GamificationService {

  private static final Logger logger =
      Logger.getLogger(GamificationService.class.getName());

  public Tracker create(Key<Account> accountKey) {
    return Tracker.builder()
        .id(accountKey.toWebSafeString() + ":tracker")
        .bounties(0)
        .points(0)
        .dailyPoints(0)
        .firstQuestionOfDay(false)
        .firstComment(false)
        .firstQuestion(false)
        .level(0)
        .build();
  }

  public Group.OfTwo<Tracker, Question> exchangeBounties(Tracker tracker, Question question) {
    tracker = tracker.addBounties(question.getBounty());
    question = question.withBounty(0);

    return Group.OfTwo.create(tracker, question);
  }
}