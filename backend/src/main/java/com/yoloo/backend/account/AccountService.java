package com.yoloo.backend.account;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.common.base.Optional;
import com.google.firebase.auth.FirebaseToken;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.util.StringUtil;
import java.util.List;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;

@NoArgsConstructor(staticName = "create")
public class AccountService {

  public AccountModel create(FirebaseToken token, String locale, Account.Gender gender,
      String topicIds, AccountShardService service) {
    final Key<Account> accountKey = factory().allocateId(Account.class);

    // Create list of new shard entities for given comment.
    List<AccountCounterShard> shards = service.createShards(accountKey);
    // Create list of new shard refs.
    List<Ref<AccountCounterShard>> shardRefs = ShardUtil.createRefs(shards)
        .toList(shards.size()).blockingGet();

    List<Key<Topic>> topicKeys = StringUtil.split(topicIds, ",")
        .map(Key::<Topic>create)
        .toList(5)
        .blockingGet();

    Account account = createAccount(token, accountKey, locale, gender, topicKeys, shardRefs);

    return AccountModel.builder()
        .account(account)
        .shards(shards)
        .build();
  }

  public Account update(Account account, Optional<String> mediaId, Optional<String> username) {
    if (username.isPresent()) {
      account = account.withUsername(username.get());
    }

    return account;
  }

  private Account createAccount(FirebaseToken token, Key<Account> accountKey, String locale,
      Account.Gender gender, List<Key<Topic>> topicKeys, List<Ref<AccountCounterShard>> shardRefs) {

    String username = token.getName().trim().replaceAll("\\s+", "").toLowerCase();

    return Account.builder()
        .id(accountKey.getId())
        .username(username)
        .realname(token.getName())
        .firebaseUUID(token.getUid())
        .email(new Email(token.getEmail()))
        .avatarUrl(new Link(token.getPicture()))
        .locale(locale)
        .gender(gender)
        .shardRefs(shardRefs)
        .topicKeys(topicKeys)
        .counts(Account.Counts.builder().build())
        .achievements(Account.Achievements.builder().build())
        .created(DateTime.now())
        .build();
  }
}