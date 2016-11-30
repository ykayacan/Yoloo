package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Cache(expirationSeconds = 60)
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentCounterShard {

    public static final int SHARD_COUNT = ShardConfig.COMMENT_SHARD_COUNTER;

    /**
     * Websafe commentId:shard_num
     */
    @Id
    private String id;

    private long votes;

    public Key<CommentCounterShard> getKey() {
        return Key.create(CommentCounterShard.class, id);
    }

    public void increaseVotes() {
        ++this.votes;
    }

    public void decreaseVotes() {
        --this.votes;
    }
}
