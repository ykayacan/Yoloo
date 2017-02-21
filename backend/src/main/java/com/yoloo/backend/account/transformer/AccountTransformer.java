package com.yoloo.backend.account.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.dto.AccountDTO;

public class AccountTransformer implements Transformer<Account, AccountDTO> {
  @Override public AccountDTO transformTo(Account in) {
    return AccountDTO.builder()
        .id(in.getWebsafeId())
        .username(in.getUsername())
        .realname(in.getRealname())
        .email(in.getEmail().getEmail())
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

  @Override public Account transformFrom(AccountDTO in) {
    return null;
  }
}
