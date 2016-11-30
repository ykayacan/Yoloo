package com.yoloo.backend.fcm;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;

import lombok.Builder;
import lombok.Value;

@Entity
@Value
@Builder(toBuilder = true)
public class RegistrationRecord {

    @Id
    private Long id;

    @Parent
    private Key<Account> userKey;

    @Index
    private String regId;
}