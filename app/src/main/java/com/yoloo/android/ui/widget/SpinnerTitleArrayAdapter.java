package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.yoloo.android.R;

public class SpinnerTitleArrayAdapter extends ArrayAdapter<String> {

  private static final int EXTRA = 1;

  private final LayoutInflater inflater;
  private String title;
  private String subtitle;

  public SpinnerTitleArrayAdapter(Context context, String[] items) {
    super(context, -1, items);
    this.inflater = LayoutInflater.from(context);
  }

  public void setHeader(String title, String subtitle) {
    this.title = title;
    this.subtitle = subtitle;
  }

  @Override public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
    return getDropdownView(position, convertView, parent);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    return getHeaderView(position, convertView, parent);
  }

  @NonNull private View getHeaderView(int position, View convertView, ViewGroup parent) {
    HeaderViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.spinner_row_head, parent, false);

      holder = new HeaderViewHolder();
      holder.tvTitle = (TextView) convertView.findViewById(R.id.spinner_title);
      holder.tvSubtitle = (TextView) convertView.findViewById(R.id.spinner_subtitle);

      convertView.setTag(holder);
    } else {
      holder = (HeaderViewHolder) convertView.getTag();
    }

    holder.tvTitle.setText(title);
    if (TextUtils.isEmpty(getItem(position))) {
      holder.tvSubtitle.setText(subtitle);
    } else {
      holder.tvSubtitle.setText(getItem(position));
    }

    return convertView;
  }

  @NonNull private View getDropdownView(int position, View convertView, ViewGroup parent) {
    final ItemViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);

      holder = new ItemViewHolder();
      holder.tvSpinnerItem = (TextView) convertView.findViewById(android.R.id.text1);

      convertView.setTag(holder);
    } else {
      holder = (ItemViewHolder) convertView.getTag();
    }

    holder.tvSpinnerItem.setText(getItem(position));

    return convertView;
  }

  @Override public int getCount() {
    return super.getCount() + EXTRA;
  }

  @Nullable @Override public String getItem(int position) {
    return position == 0 ? null : super.getItem(position - EXTRA);
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getViewTypeCount() {
    return 1;
  }

  @Override public long getItemId(int position) {
    return position >= EXTRA ? super.getItemId(position - EXTRA) : position - EXTRA;
  }

  @Override public boolean isEnabled(int position) {
    return position != 0;
  }

  private static class HeaderViewHolder {
    TextView tvTitle;
    TextView tvSubtitle;
  }

  private static class ItemViewHolder {
    TextView tvSpinnerItem;
  }
}
