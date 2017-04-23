package com.yoloo.android.feature.editor.tagselectdialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.feature.category.ChipAdapter;
import com.yoloo.android.framework.MvpAlertDialog;
import com.yoloo.android.ui.recyclerview.decoration.SpacingItemDecoration;
import com.yoloo.android.ui.widget.TagAutoCompleteTextView;
import com.yoloo.android.util.DisplayUtil;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class TagSelectDialog extends MvpAlertDialog<TagSelectDialogView, TagSelectDialogPresenter>
    implements TagSelectDialogView, ChipAdapter.OnItemSelectListener<TagRealm> {

  //@BindView(R.id.tv_tag_autocomplete) NachoTextView tvTagAutoComplete;
  @BindView(R.id.rv_tags) RecyclerView rvTags;
  @BindView(R.id.tv_add_tag) TextView tvAddTag;
  @BindView(R.id.flexbox_tags) FlexboxLayout flexboxLayout;

  @BindString(android.R.string.ok) String ok;
  @BindString(android.R.string.cancel) String cancel;

  private ChipAdapter<TagRealm> chipAdapter;

  private List<TagRealm> selectedTags;

  private int selectedTagCounter = 0;

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

    selectedTags = new ArrayList<>(7);

    //setupChipTextView();

    setDialogButtons();

    setupRecyclerView();
  }

  /*private void showDropdown() {
    final int tagSize = tvTagAutoComplete.getAllChips().size();
    if (tagSize <= 7) {
      tagDropdownRunnable = tvTagAutoComplete::showDropDown;
    }
  }*/

  private void setDialogButtons() {
    setButton(BUTTON_POSITIVE, ok,
        (dialog, which) -> onTagsSelectedListener.onTagsSelected(selectedTags));
    setButton(BUTTON_NEGATIVE, cancel, (dialog, which) -> {
    });
  }

  @NonNull
  @Override
  public TagSelectDialogPresenter createPresenter() {
    return new TagSelectDialogPresenter(TagRepository.getInstance(TagRemoteDataStore.getInstance(),
        TagDiskDataStore.getInstance()));
  }

  @Override
  public void onRecommendedTagsLoaded(List<TagRealm> tags) {
    chipAdapter.addChipItems(tags);
  }

  @Override
  public void onSearchTags(List<TagRealm> tags) {
    /*List<TagRealm> filteredTags = Stream
        .of(tags)
        .filterNot(tag -> tvTagAutoComplete.getChipValues().contains(tag.getName()))
        .toList();*/
    /*adapter.replaceItems(tags);
    handler.post(tagDropdownRunnable);*/
  }

  @Override
  public void onItemSelect(View v, EpoxyModel<?> model, TagRealm item, boolean selected) {
    if (selectedTags.size() == 7) {
      Toast.makeText(getContext(), "You have selected 7 tags", Toast.LENGTH_SHORT).show();
      return;
    }

    if (selectedTags.contains(item)) {
      selectedTags.remove(item);
      --selectedTagCounter;
    } else {
      selectedTags.add(item);
      ++selectedTagCounter;
    }

    /*tags.clear();
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

    convertToTagChips(tags);*/
  }

  @OnClick(R.id.tv_add_tag)
  void addTag() {
    Timber.d("Selected siz: %s", selectedTags.size());
    if (selectedTagCounter == 7) {
      Toast.makeText(getContext(), "You have selected 7 tags", Toast.LENGTH_SHORT).show();
    } else {
      TagAutoCompleteTextView tag =
          new TagAutoCompleteTextView(getContext().getApplicationContext());

      flexboxLayout.addView(tag, 0);
      ++selectedTagCounter;
    }
  }

  public void setInitialTags(List<String> tags) {
    //convertToTagChips(tags);
  }

  /*private void convertToTagChips(List<String> tags) {
    tvTagAutoComplete.setText(Stream.of(tags).collect(Collectors.joining(" ")));
    tvTagAutoComplete.setSelection(tvTagAutoComplete.getText().length());
    tvTagAutoComplete.chipifyAllUnterminatedTokens();
  }*/

  /*private void setupChipTextView() {
    adapter = new AutoCompleteTagAdapter(getContext());
    tvTagAutoComplete.setAdapter(adapter);
    tvTagAutoComplete.setIllegalCharacters('\"', '.', '~');
    tvTagAutoComplete.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
    tvTagAutoComplete.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
    tvTagAutoComplete.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
    tvTagAutoComplete.setNachoValidator(new ChipifyingNachoValidator());
    tvTagAutoComplete.enableEditChipOnTouch(true, true);
  }*/

  private void setupRecyclerView() {
    chipAdapter = new ChipAdapter<>(this);
    //chipAdapter.setBackgroundDrawable(R.drawable.dialog_tag_bg);

    final FlexboxLayoutManager lm = new FlexboxLayoutManager();
    lm.setFlexDirection(FlexDirection.ROW);
    lm.setJustifyContent(JustifyContent.CENTER);

    final int spacing = DisplayUtil.dpToPx(4);

    rvTags.setLayoutManager(lm);
    SimpleItemAnimator animator = new DefaultItemAnimator();
    rvTags.setItemAnimator(animator);
    rvTags.addItemDecoration(new SpacingItemDecoration(spacing, spacing));
    rvTags.setHasFixedSize(true);
    rvTags.setAdapter(chipAdapter);
  }

  public void setOnTagsSelectedListener(OnTagsSelectedListener onTagsSelectedListener) {
    this.onTagsSelectedListener = onTagsSelectedListener;
  }

  public interface OnTagsSelectedListener {
    void onTagsSelected(List<TagRealm> tags);
  }
}
