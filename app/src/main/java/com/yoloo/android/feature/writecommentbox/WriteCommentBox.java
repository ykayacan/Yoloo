package com.yoloo.android.feature.writecommentbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpLinearLayout;
import com.yoloo.android.ui.tokenizer.SpaceTokenizer;
import com.yoloo.android.ui.widget.AutoCompleteMentionAdapter;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.Preconditions;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class WriteCommentBox extends MvpLinearLayout<WriteCommentBoxView, WriteCommentBoxPresenter>
    implements WriteCommentBoxView {

  private static final int AUTOCOMPLETE_DELAY = 400;

  @BindView(R.id.tv_input_comment) MultiAutoCompleteTextView tvCompose;
  @BindColor(R.color.background_light) int lightBgColor;

  private String postId;

  private AutoCompleteMentionAdapter mentionAdapter;
  private Runnable mentionDropdownRunnable = () -> tvCompose.showDropDown();

  private NewCommentListener newCommentListener;

  public WriteCommentBox(Context context) {
    super(context);
    init();
  }

  public WriteCommentBox(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public WriteCommentBox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    final View view = inflate(getContext(), R.layout.layout_write_comment_box, this);
    ButterKnife.bind(this, view);

    if (isInEditMode()) {
      return;
    }

    setBackgroundColor(lightBgColor);
    setOrientation(LinearLayout.HORIZONTAL);

    setupMentionsAdapter();

    mentionAdapter.getQuery()
        .debounce(AUTOCOMPLETE_DELAY, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(query -> getPresenter().suggestUsers(query));
  }

  @Override public void onSuggestionsLoaded(List<AccountRealm> suggestions) {
    mentionAdapter.replaceItems(suggestions);
    tvCompose.post(mentionDropdownRunnable);
  }

  @Override public void onNewComment(CommentRealm comment) {
    newCommentListener.onNewComment(comment);
  }

  @Override public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull @Override public WriteCommentBoxPresenter createPresenter() {
    return new WriteCommentBoxPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        CommentRepository.getInstance(
            CommentRemoteDataStore.getInstance(),
            CommentDiskDataStore.getInstance()));
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public void setNewCommentListener(NewCommentListener newCommentListener) {
    this.newCommentListener = newCommentListener;
  }

  public void showKeyboard() {
    tvCompose.requestFocus();
    KeyboardUtil.showDelayedKeyboard(tvCompose);
  }

  public void hideKeyboard() {
    KeyboardUtil.hideKeyboard(tvCompose);
  }

  @OnClick(R.id.btn_send_comment) void sendComment() {
    final String content = tvCompose.getText().toString().trim();

    if (TextUtils.isEmpty(content)) {
      return;
    }

    Preconditions.checkNotNull(postId, "PostId can not be null!");
    Preconditions.checkNotNull(newCommentListener, "NewCommentListener is null!");

    CommentRealm comment = new CommentRealm()
        .setId(UUID.randomUUID().toString())
        .setContent(content)
        .setCreated(new Date())
        .setPostId(postId);

    getPresenter().sendComment(comment);

    tvCompose.setText("");
  }

  private void setupMentionsAdapter() {
    mentionAdapter = new AutoCompleteMentionAdapter(getContext());
    tvCompose.setAdapter(mentionAdapter);
    tvCompose.setTokenizer(new SpaceTokenizer());
  }

  public interface NewCommentListener {
    void onNewComment(CommentRealm comment);
  }
}
