package com.yoloo.android.feature.editor.tagselectdialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.feature.category.ChipAdapter;
import com.yoloo.android.framework.MvpAlertDialog;
import com.yoloo.android.ui.recyclerview.decoration.SpacingItemDecoration;
import com.yoloo.android.ui.widget.AutoCompleteTagAdapter;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TagSelectDialog extends MvpAlertDialog<TagSelectDialogView, TagSelectDialogPresenter>
    implements TagSelectDialogView, ChipAdapter.OnItemSelectListener<TagRealm> {

  @BindView(R.id.tv_tag_autocomplete) NachoTextView tvTagAutoComplete;
  @BindView(R.id.rv_tags) RecyclerView rvTags;

  @BindString(android.R.string.ok) String ok;
  @BindString(android.R.string.cancel) String cancel;

  private AutoCompleteTagAdapter tagAutocompleteAdapter;
  private ChipAdapter<TagRealm> chipAdapter;

  private WeakHandler handler;
  private Runnable tagDropdownRunnable;

  private List<String> tags;

  private OnTagsSelectedListener onTagsSelectedListener;

  public TagSelectDialog(@NonNull Context context) {
    super(context);
    init();
  }

  public TagSelectDialog(@NonNull Context context, @StyleRes int themeResId) {
    super(context, themeResId);
    init();
  }

  public TagSelectDialog(@NonNull Context context, boolean cancelable,
      @Nullable OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
    init();
  }

  private void init() {
    final View view = View.inflate(getContext(), R.layout.dialog_editor_tag, null);
    setView(view);
    ButterKnife.bind(this, view);

    tags = new ArrayList<>(7);

    handler = new WeakHandler();

    setupChipTextView();
    setCancelable(false);

    setDialogButtons();

    showDropdown();

    tagAutocompleteAdapter.getQuery()
        .debounce(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> getPresenter().recommendTags(s));

    setupRecyclerView();
  }

  private void showDropdown() {
    final int tagSize = tvTagAutoComplete.getAllChips().size();
    if (tagSize <= 7) {
      tagDropdownRunnable = tvTagAutoComplete::showDropDown;
    }
  }

  private void setDialogButtons() {
    setButton(BUTTON_POSITIVE, ok, (dialog, which) -> onTagsSelectedListener.onTagsSelected(
        tvTagAutoComplete.getChipValues()));
    setButton(BUTTON_NEGATIVE, cancel, (dialog, which) -> {
    });
  }

  @NonNull @Override public TagSelectDialogPresenter createPresenter() {
    return new TagSelectDialogPresenter();
  }

  @Override public void onRecommendedTagsLoaded(List<TagRealm> tags) {
    chipAdapter.addChipItems(tags);
  }

  @Override public void onNewRecommendedTagsLoaded(List<TagRealm> tags) {
    List<TagRealm> filteredTags = Stream.of(tags)
        .filterNot(tag -> tvTagAutoComplete.getChipValues().contains(tag.getName()))
        .toList();
    tagAutocompleteAdapter.replaceItems(filteredTags);
    handler.post(tagDropdownRunnable);
  }

  @Override public void onItemSelect(View v, EpoxyModel<?> model, TagRealm item, boolean selected) {
    tags.clear();
    tags.addAll(tvTagAutoComplete.getChipValues());
    if (selected) {
      if (tags.contains(item.getName())) {
        chipAdapter.toggleSelection(model);
      } else {
        tags.add(item.getName());
      }
    } else {
      tags.remove(item.getName());
    }

    convertToTagChips(tags);
  }

  public void setInitialTags(List<String> tags) {
    convertToTagChips(tags);
  }

  private void convertToTagChips(List<String> tags) {
    tvTagAutoComplete.setText(Stream.of(tags).collect(Collectors.joining(" ")));
    tvTagAutoComplete.setSelection(tvTagAutoComplete.getText().length());
    tvTagAutoComplete.chipifyAllUnterminatedTokens();
  }

  private void setupChipTextView() {
    tagAutocompleteAdapter = new AutoCompleteTagAdapter(getContext());
    tvTagAutoComplete.setAdapter(tagAutocompleteAdapter);
    tvTagAutoComplete.setIllegalCharacters('\"', '.', '~');
    tvTagAutoComplete.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
    tvTagAutoComplete.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
    tvTagAutoComplete.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
    tvTagAutoComplete.setNachoValidator(new ChipifyingNachoValidator());
    tvTagAutoComplete.enableEditChipOnTouch(true, true);
  }

  private void setupRecyclerView() {
    chipAdapter = new ChipAdapter<>(this);
    chipAdapter.setBackgroundDrawable(R.drawable.dialog_tag_bg);

    final FlexboxLayoutManager lm = new FlexboxLayoutManager();
    lm.setFlexDirection(FlexDirection.ROW);
    lm.setJustifyContent(JustifyContent.CENTER);

    final int spacing = DisplayUtil.dpToPx(4);

    rvTags.setLayoutManager(lm);
    SimpleItemAnimator animator = new DefaultItemAnimator();
    animator.setChangeDuration(0L);
    rvTags.setItemAnimator(animator);
    rvTags.addItemDecoration(new SpacingItemDecoration(spacing, spacing));
    rvTags.setHasFixedSize(true);
    rvTags.setAdapter(chipAdapter);
  }

  public void setOnTagsSelectedListener(OnTagsSelectedListener onTagsSelectedListener) {
    this.onTagsSelectedListener = onTagsSelectedListener;
  }

  public interface OnTagsSelectedListener {
    void onTagsSelected(List<String> tagNames);
  }
}
