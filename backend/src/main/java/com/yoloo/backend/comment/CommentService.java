package com.yoloo.backend.comment;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class CommentService {

    public Comment create(Account account, Key<Question> questionKey, String content,
                          CommentShardService service) {
        final Key<Comment> commentKey = ofy().factory()
                .allocateId(account.getKey(), Comment.class);

        return Comment.builder()
                .id(commentKey.getId())
                .parentUser(Ref.create(account))
                .questionKey(questionKey)
                .content(content)
                .username(account.getUsername())
                .avatarUrl(account.getAvatarUrl())
                .dir(Vote.Direction.DEFAULT)
                .shardKeys(service.createShardKeys(commentKey))
                .accepted(false)
                .votes(0)
                .created(DateTime.now())
                .build();
    }

    public Comment update(Comment comment, Optional<String> content,
                          Optional<Boolean> accepted) {
        if (content.isPresent()) {
            comment = comment.withContent(content.get());
        }

        if (accepted.isPresent()) {
            comment = comment.withAccepted(accepted.get());
        }

        return comment;
    }

    public Pair<Question, Comment> accept(Question question, Comment comment,
                                          Optional<Boolean> accepted) {
        if (accepted.isPresent()) {
            question = question.withAcceptedComment(Ref.create(comment.getKey()));
            comment = comment.withAccepted(true);
        }

        return Pair.of(question, comment);
    }

    public ImmutableList<Key<Vote>> getVoteKeys(Collection<Key<Comment>> commentKeys) {
        return Observable.fromIterable(commentKeys)
                .map(new Function<Key<Comment>, List<Key<Vote>>>() {
                    @Override
                    public List<Key<Vote>> apply(Key<Comment> key) throws Exception {
                        return getVoteKeys(key);
                    }
                })
                .toList()
                .to(new Function<Single<List<List<Key<Vote>>>>, ImmutableList<Key<Vote>>>() {
                    @Override
                    public ImmutableList<Key<Vote>> apply(Single<List<List<Key<Vote>>>> listSingle) throws Exception {
                        ImmutableList.Builder<Key<Vote>> builder = ImmutableList.builder();

                        for (List<Key<Vote>> keys : listSingle.blockingGet()) {
                            builder = builder.addAll(keys);
                        }

                        return builder.build();
                    }
                });
    }

    public ImmutableList<Key<Vote>> getVoteKeys(Key<Comment> commentKey) {
        return ImmutableList.copyOf(ofy().load().type(Vote.class)
                .filter(Vote.FIELD_VOTABLE_KEY + " =", commentKey)
                .keys().list());
    }
}
