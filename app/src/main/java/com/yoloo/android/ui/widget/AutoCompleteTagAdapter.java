package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.yoloo.android.R;
import com.yoloo.android.data.model.TagRealm;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTagAdapter extends ArrayAdapter<TagRealm> {

  private final PublishSubject<String> subject = PublishSubject.create();

  private final List<TagRealm> items;

  private LayoutInflater inflater;

  public AutoCompleteTagAdapter(Context context) {
    super(context, 0);
    this.items = new ArrayList<>(5);
    this.inflater = LayoutInflater.from(context);
  }

  @Nullable
  @Override
  public TagRealm getItem(int position) {
    return items.get(position);
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @NonNull
  @Override
  public Filter getFilter() {
    return new TagFilter(items, this, subject);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    // Get the data item from filtered searchUser.
    final TagRealm item = getItem(position);

    TagViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_autocomplete_tag, parent, false);

      holder = new TagViewHolder();
      holder.tvTag = (TextView) convertView.findViewById(R.id.tv_tag);

      convertView.setTag(holder);
    } else {
      holder = (TagViewHolder) convertView.getTag();
    }

    final String tag = "# " + item.getName();
    holder.tvTag.setText(tag);

    return convertView;
  }

  public void replaceItems(List<TagRealm> tags) {
    this.items.clear();
    this.items.addAll(tags);
  }

  public Observable<String> getQuery() {
    return subject;
  }

  private static class TagViewHolder {
    TextView tvTag;
  }

  private static class TagFilter extends Filter {

    private final List<TagRealm> items;
    private final AutoCompleteTagAdapter adapter;
    private final PublishSubject<String> subject;

    private TagFilter(List<TagRealm> items, AutoCompleteTagAdapter adapter,
        PublishSubject<String> subject) {
      this.items = items;
      this.adapter = adapter;
      this.subject = subject;
    }

    @Override protected FilterResults performFiltering(CharSequence constraint) {
      items.clear();

      if (isValid(constraint)) {
        final String query = constraint.toString().toLowerCase().trim();
        subject.onNext(query);
      }
      return null;
    }

    @Override protected void publishResults(CharSequence constraint, FilterResults results) {
      if (items != null && !items.isEmpty()) {
        adapter.notifyDataSetChanged();
      }
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
      if (resultValue instanceof TagRealm) {
        return ((TagRealm) resultValue).getName();
      }
      return super.convertResultToString(resultValue);
    }

    private boolean isValid(CharSequence constraint) {
      return !TextUtils.isEmpty(constraint) && constraint.length() > 2;
    }
  }
}
