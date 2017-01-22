package com.yoloo.android.feature.ui.widget;

import android.content.Context;
import android.content.res.Resources;
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
import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTagAdapter extends ArrayAdapter<TagRealm> {

  private final OnAutoCompleteListener onAutoCompleteListener;
  private final List<TagRealm> items;

  private LayoutInflater inflater;
  private Resources res;

  public AutoCompleteTagAdapter(Context context, OnAutoCompleteListener onAutoCompleteListener) {
    super(context, 0);
    this.res = context.getResources();
    this.onAutoCompleteListener = onAutoCompleteListener;
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
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        items.clear();

        if (isConditionsValid(constraint)) {
          final String filtered = constraint.toString().toLowerCase().trim();
          onAutoCompleteListener.onAutoCompleteFilter(filtered);
        }
        return null;
      }

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        if (items != null && !items.isEmpty()) {
          notifyDataSetChanged();
        }
      }

      @Override
      public CharSequence convertResultToString(Object resultValue) {
        if (resultValue instanceof TagRealm) {
          return ((TagRealm) resultValue).getName();
        }
        return super.convertResultToString(resultValue);
      }
    };
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    // Get the data item from filtered list.
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

    holder.tvTag.setText(res.getString(R.string.label_tag, item.getName()));

    return convertView;
  }

  public void replaceItems(List<TagRealm> tags) {
    this.items.clear();
    this.items.addAll(tags);
  }

  private boolean isConditionsValid(CharSequence constraint) {
    return !TextUtils.isEmpty(constraint) && constraint.length() > 2;
  }

  public interface OnAutoCompleteListener {
    void onAutoCompleteFilter(String filtered);
  }

  private static class TagViewHolder {
    TextView tvTag;
  }
}