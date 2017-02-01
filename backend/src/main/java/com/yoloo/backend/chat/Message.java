package com.yoloo.backend.chat;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.account.Account;
import java.util.Set;
import lombok.Value;
import org.joda.time.DateTime;

@Value
public class Message {

  @Id
  private long id;

  private String content;

  private Set<Key<Account>> recipients;

  private DateTime created;
}
