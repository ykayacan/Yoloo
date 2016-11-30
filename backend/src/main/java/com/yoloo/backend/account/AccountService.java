package com.yoloo.backend.account;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.firebase.auth.FirebaseToken;

import com.googlecode.objectify.Key;

import org.joda.time.DateTime;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
public class AccountService {

    public Account create(Key<Account> accountKey, FirebaseToken token) {
        return Account.builder()
                .id(accountKey.getId())
                .username(token.getName())
                .email(new Email(token.getEmail()))
                .avatarUrl(new Link(token.getPicture()))
                .followings(0)
                .followers(0)
                .questions(0)
                .created(DateTime.now())
                .build();
    }

    public AccountDetail createDetail(Key<Account> accountKey) {
        return null;
    }
}