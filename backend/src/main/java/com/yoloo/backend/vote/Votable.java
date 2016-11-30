package com.yoloo.backend.vote;

import com.googlecode.objectify.Key;

public interface Votable {

    <T> Key<T> getVotableKey();
}
