package com.yoloo.backend.question;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

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
public final class QuestionCounterShard {

    public static final int SHARD_COUNT = ShardConfig.FORUM_SHARD_COUNTER;

    /**
     * Websafe questionId:shard_num
     */
    @Id
    private String id;

    @Min(0)
    private long comments;

    private long votes;

    @Size(max = 3)
    private long reports;

    public Key<QuestionCounterShard> getKey() {
        return Key.create(QuestionCounterShard.class, id);
    }

    public long addVotes(long votes) {
        this.votes += votes;
        return this.votes;
    }

    public void increaseVotes() {
        this.votes++;
    }

    public void decreaseVotes() {
        this.votes--;
    }

    public void increaseComments() {
        this.comments++;
    }

    public void decreaseComments() {
        this.comments--;
    }
}