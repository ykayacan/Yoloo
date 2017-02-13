package com.yoloo.android.feature.write.tagoverview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.View;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.ui.widget.AutoCompleteTagAdapter;
import com.yoloo.android.ui.widget.tagview.TagView;
import com.yoloo.android.framework.MvpAlertDialog;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class TagOverviewDialog extends MvpAlertDialog<TagOverviewView, TagOverviewPresenter>
    implements TagOverviewView {

  private static final TagView.DataTransform<TagRealm> TRANSFORMER = TagRealm::getName;

  @BindView(R.id.tv_tag_autocomplete) NachoTextView tvTagAutoComplete;
  @BindView(R.id.tagview_overlay_recommended_tags) TagView tagView;

  @BindString(android.R.string.ok) String ok;
  @BindString(android.R.string.cancel) String cancel;

  private AutoCompleteTagAdapter adapter;

  private WeakHandler handler;
  private Runnable tagDropdownRunnable;

  public TagOverviewDialog(@NonNull Context context) {
    super(context);
    init();
  }

  public TagOverviewDialog(@NonNull Context context, @StyleRes int themeResId) {
    super(context, themeResId);
    init();
  }

  public TagOverviewDialog(@NonNull Context context, boolean cancelable,
      @Nullable OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
    init();
  }

  private void init() {
    final View view = View.inflate(getContext(), R.layout.dialog_editor_tag, null);
    setView(view);
    ButterKnife.bind(this, view);

    handler = new WeakHandler();

    setupChipTextView();
    setCancelable(false);
    setButton(BUTTON_POSITIVE, ok, (dialog, which) -> {

    });
    setButton(BUTTON_NEGATIVE, cancel, (dialog, which) -> {
    });

    final int tagSize = tvTagAutoComplete.getAllChips().size();
    if (tagSize <= 7) {
      tagDropdownRunnable = tvTagAutoComplete::showDropDown;
    } else {

    }

    adapter.getQuery()
        .debounce(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> getPresenter().recommendTags(s));

    tagView.addOnTagSelectListener((item, selected) -> {
      String name = ((TagRealm)item).getName();
      if (selected) {
        tvTagAutoComplete.setText(name);
        tvTagAutoComplete.chipifyAllUnterminatedTokens();
      }
    });
  }

  @NonNull @Override public TagOverviewPresenter createPresenter() {
    return new TagOverviewPresenter();
  }

  @Override public void onRecommendedTagsLoaded(List<TagRealm> tags) {
    tagView.setData(tags, TRANSFORMER);
  }

  @Override public void onNewRecommendedTagsLoaded(List<TagRealm> tags) {
    adapter.replaceItems(tags);
    handler.post(tagDropdownRunnable);
  }

  private void setupChipTextView() {
    adapter = new AutoCompleteTagAdapter(getContext());
    tvTagAutoComplete.setAdapter(adapter);
    tvTagAutoComplete.setIllegalCharacters('\"', '.', '~');
    tvTagAutoComplete.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
    tvTagAutoComplete.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
    tvTagAutoComplete.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
    tvTagAutoComplete.setNachoValidator(new ChipifyingNachoValidator());
    tvTagAutoComplete.enableEditChipOnTouch(true, true);
    tvTagAutoComplete.setOnChipClickListener(
        (chip, motionEvent) -> Timber.d("onChipClick: " + chip.getText()));

  }
}
