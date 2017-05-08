package com.yoloo.android.feature.writecommentbox;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpLinearLayout;
import com.yoloo.android.ui.tokenizer.SpaceTokenizer;
import com.yoloo.android.ui.widget.AutoCompleteMentionAdapter;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.Preconditions;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class CommentAutocomplete
    extends MvpLinearLayout<CommentAutocompleteView, CommentAutocompletePresenter>
    implements CommentAutocompleteView {

  private static final int AUTOCOMPLETE_DELAY = 400;

  @BindView(R.id.tv_comment) MultiAutoCompleteTextView tvComment;
  @BindDrawable(R.drawable.comment_bg) Drawable background;

  private String postId;

  private AccountRealm me;

  private RecyclerView recyclerView;

  private AutoCompleteMentionAdapter mentionAdapter;
  private Runnable mentionDropdownRunnable = () -> tvComment.showDropDown();

  private NewCommentListener newCommentListener;

  public CommentAutocomplete(Context context) {
    super(context);
    init();
  }

  public CommentAutocomplete(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CommentAutocomplete(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    final View view = inflate(getContext(), R.layout.layout_comment_autocomplete, this);
    ButterKnife.bind(this, view);

    setBackground(background);
    setOrientation(LinearLayout.HORIZONTAL);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    if (isInEditMode()) {
      return;
    }

    setupMentionsAdapter();

    /*mentionAdapter
        .getQuery()
        .filter(s -> !s.isEmpty())
        .map(s -> s.substring(1))
        .debounce(AUTOCOMPLETE_DELAY, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(query -> getPresenter().suggestUsers(query));*/
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (isInEditMode()) {
      return;
    }
    getPresenter().loadMe();
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    this.me = me;
  }

  @Override
  public void onSuggestionsLoaded(List<AccountRealm> suggestions) {
    mentionAdapter.replaceItems(suggestions);
    tvComment.post(mentionDropdownRunnable);
  }

  @Override
  public void onNewComment(CommentRealm comment) {
    newCommentListener.onNewComment(comment);
  }

  @Override
  public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull
  @Override
  public CommentAutocompletePresenter createPresenter() {
    return new CommentAutocompletePresenter(UserRepositoryProvider.getRepository(),
        CommentRepositoryProvider.getRepository());
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public void setNewCommentListener(NewCommentListener newCommentListener) {
    this.newCommentListener = newCommentListener;
  }

  public void setRecyclerView(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
  }

  public void showKeyboard() {
    tvComment.requestFocus();
    KeyboardUtil.showDelayedKeyboard(tvComment);
  }

  @OnClick(R.id.btn_send_comment)
  void sendComment() {
    final String content = tvComment.getText().toString().trim();

    if (TextUtils.isEmpty(content)) {
      return;
    }

    Preconditions.checkNotNull(postId, "postId can not be null!");
    Preconditions.checkNotNull(newCommentListener, "NewCommentListener is null!");

    CommentRealm comment = new CommentRealm()
        .setId(UUID.randomUUID().toString())
        .setPostId(postId)
        .setOwnerId(me.getId())
        .setUsername(me.getUsername())
        .setAvatarUrl(me.getAvatarUrl())
        .setContent(content)
        .setCreated(new Date());

    KeyboardUtil.hideKeyboard(getRootView());

    getPresenter().sendComment(comment);

    tvComment.setText("");
  }

  private void setupMentionsAdapter() {
    mentionAdapter = new AutoCompleteMentionAdapter(getContext());
    tvComment.setAdapter(mentionAdapter);
    tvComment.setTokenizer(new SpaceTokenizer());
  }

  public interface NewCommentListener {
    void onNewComment(CommentRealm comment);
  }
}
