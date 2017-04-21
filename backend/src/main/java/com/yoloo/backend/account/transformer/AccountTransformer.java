package com.yoloo.backend.account.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.dto.AccountDTO;
import org.joda.time.DateTime;
import org.joda.time.Years;

public class AccountTransformer implements Transformer<Account, AccountDTO> {

  @Override
  public AccountDTO transformTo(Account in) {
    return AccountDTO
        .builder()
        .id(in.getWebsafeId())
        .username(in.getUsername())
        .realname(in.getRealname())
        .gender(in.getGender() != null ? in.getGender().name().toLowerCase() : null)
        .age(in.getBirthDate() == null
            ? 0
            : Years.yearsBetween(in.getBirthDate(), DateTime.now()).getYears())
        .websiteUrl(in.getWebsiteUrl() != null ? in.getWebsiteUrl().getValue() : null)
        .bio(in.getBio())
        .email(in.getEmail().getEmail())
        .country(in.getCountry())
        .visitedCountries(in.getVisitedCountries())
        .subscribedGroupIds(in.getSubscribedGroupIds())
        .avatarUrl(in.getAvatarUrl() != null ? in.getAvatarUrl().getValue() : null)
        .created(in.getCreated().toDate())
        .locale(in.getLocale())
        .isFollowing(in.isFollowing())
        .followingCount(in.getCounts() != null ? in.getCounts().getFollowings() : 0L)
        .followerCount(in.getCounts() != null ? in.getCounts().getFollowers() : 0L)
        .postCount(in.getCounts() != null ? in.getCounts().getQuestions() : 0L)
        .level(in.getDetail() != null ? in.getDetail().getLevel() : 0)
        .bountyCount(in.getDetail() != null ? in.getDetail().getBounties() : 0)
        .pointCount(in.getDetail() != null ? in.getDetail().getPoints() : 0)
        .build();
  }

  @Override
  public Account transformFrom(AccountDTO in) {
    return null;
  }
}
