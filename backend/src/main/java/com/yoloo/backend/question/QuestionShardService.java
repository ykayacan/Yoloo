package com.yoloo.backend.question;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.question.QuestionCounterShard.SHARD_COUNT;

@AllArgsConstructor(staticName = "newInstance")
public class QuestionShardService {

    public ImmutableList<QuestionCounterShard> createShards(final Key<Question> postKey) {
        return Observable.range(1, SHARD_COUNT)
                .map(new Function<Integer, QuestionCounterShard>() {
                    @Override
                    public QuestionCounterShard apply(Integer shardId) throws Exception {
                        return createShard(postKey, shardId);
                    }
                })
                .to(new Function<Observable<QuestionCounterShard>,
                        ImmutableList<QuestionCounterShard>>() {
                    @Override
                    public ImmutableList<QuestionCounterShard> apply(
                            Observable<QuestionCounterShard> o)
                            throws Exception {
                        return ImmutableList.copyOf(o.toList().blockingGet());
                    }
                });
    }

    public QuestionCounterShard createShard(Key<Question> postKey, int shardId) {
        return QuestionCounterShard.builder()
                .id(QuestionUtil.createShardId(postKey, shardId))
                .comments(0)
                .reports(0)
                .votes(0)
                .build();
    }

    public ImmutableSet<Key<QuestionCounterShard>> getShardKeys(Collection<Question> questions) {
        return Observable.fromIterable(questions)
                .map(new Function<Question, Set<Key<QuestionCounterShard>>>() {
                    @Override
                    public Set<Key<QuestionCounterShard>> apply(Question question)
                            throws Exception {
                        return ImmutableSet.copyOf(question.getShardKeys());
                    }
                })
                .toList()
                .to(new Function<Single<List<Set<Key<QuestionCounterShard>>>>,
                        ImmutableSet<Key<QuestionCounterShard>>>() {
                    @Override
                    public ImmutableSet<Key<QuestionCounterShard>> apply(
                            Single<List<Set<Key<QuestionCounterShard>>>> listSingle)
                            throws Exception {
                        ImmutableSet.Builder<Key<QuestionCounterShard>> builder =
                                ImmutableSet.builder();

                        for (Set<Key<QuestionCounterShard>> keys : listSingle.blockingGet()) {
                            builder = builder.addAll(keys);
                        }
                        return builder.build();
                    }
                });
    }

    public ImmutableList<Key<QuestionCounterShard>> getShardKeys(Key<Question> key) {
        return Observable.just(key)
                .map(new Function<Key<Question>, List<Key<QuestionCounterShard>>>() {
                    @Override
                    public List<Key<QuestionCounterShard>> apply(
                            final Key<Question> questionKey) throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<QuestionCounterShard>>() {
                                    @Override
                                    public Key<QuestionCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                QuestionCounterShard.class,
                                                QuestionUtil.createShardId(questionKey, shardId));
                                    }
                                }).toList().blockingGet();
                    }
                })
                .to(new Function<Observable<List<Key<QuestionCounterShard>>>,
                        ImmutableList<Key<QuestionCounterShard>>>() {
                    @Override
                    public ImmutableList<Key<QuestionCounterShard>> apply(
                            Observable<List<Key<QuestionCounterShard>>> listObservable)
                            throws Exception {
                        return ImmutableList.copyOf(listObservable.blockingFirst());
                    }
                });
    }

    public Key<QuestionCounterShard> getRandomShardKey(final Key<Question> postKey) {
        final int shardNum = new Random().nextInt(QuestionCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return Key.create(QuestionCounterShard.class, QuestionUtil.createShardId(postKey, shardNum));
    }
}