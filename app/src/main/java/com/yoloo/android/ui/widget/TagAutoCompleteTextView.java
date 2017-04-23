package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class TagAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  private AutoCompleteTagAdapter adapter;
  private WeakHandler handler;

  public TagAutoCompleteTextView(Context context) {
    super(context);
    init();
  }

  public TagAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TagAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    handler = new WeakHandler();

    adapter = new AutoCompleteTagAdapter(getContext());

    setHint(R.string.tag_hint);
    //setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_tag_bg));
    setTextSize(14F);
    setAdapter(adapter);

    TagRepository tagRepository =
        TagRepository.getInstance(TagRemoteDataStore.getInstance(), TagDiskDataStore.getInstance());

    adapter
        .getQuery()
        .debounce(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap(query -> tagRepository.searchTag(query, null, 5))
        .observeOn(AndroidSchedulers.mainThread())
        .map(Response::getData)
        .subscribe(tags -> {
          Timber.d("Tags: %s", tags);
          adapter.replaceItems(tags);
          handler.post(this::showDropDown);
        });
  }
}
