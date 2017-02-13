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
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.util.glide.CropCircleTransformation;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;

public class AutoCompleteMentionAdapter extends ArrayAdapter<AccountRealm> {

  private final PublishSubject<String> subject = PublishSubject.create();

  private final List<AccountRealm> items;

  private LayoutInflater inflater;

  public AutoCompleteMentionAdapter(Context context) {
    super(context, 0);
    this.items = new ArrayList<>(5);
    this.inflater = LayoutInflater.from(context);
  }

  @Nullable
  @Override
  public AccountRealm getItem(int position) {
    return items.get(position);
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @NonNull
  @Override
  public Filter getFilter() {
    return new MentionFilter(items, this, subject);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    // Get the data item from filtered searchUser.
    final AccountRealm item = getItem(position);

    MentionViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_autocomplete_mention, parent, false);

      holder = new MentionViewHolder();
      holder.ivAvatar = (ImageView) convertView.findViewById(R.id.image_avatar);
      holder.tvUsername = (TextView) convertView.findViewById(R.id.text_username);

      convertView.setTag(holder);
    } else {
      holder = (MentionViewHolder) convertView.getTag();
    }

    Glide.with(getContext())
        .load(item.getAvatarUrl())
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .bitmapTransform(CropCircleTransformation.getInstance(getContext()))
        .into(holder.ivAvatar);

    holder.tvUsername.setText(item.getUsername());

    return convertView;
  }

  public void replaceItems(List<AccountRealm> accounts) {
    this.items.clear();
    this.items.addAll(accounts);
  }

  public Observable<String> getQuery() {
    return subject;
  }

  private static class MentionViewHolder {
    ImageView ivAvatar;
    TextView tvUsername;
  }

  private static class MentionFilter extends Filter {

    private final List<AccountRealm> items;
    private final AutoCompleteMentionAdapter adapter;
    private final PublishSubject<String> subject;

    private MentionFilter(List<AccountRealm> items, AutoCompleteMentionAdapter adapter,
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
      if (resultValue instanceof AccountRealm) {
        return ((AccountRealm) resultValue).toMention();
      }
      return super.convertResultToString(resultValue);
    }

    private boolean isValid(CharSequence constraint) {
      return !TextUtils.isEmpty(constraint)
          && hasMentionSymbol(constraint)
          && constraint.length() > 2;
    }

    private boolean hasMentionSymbol(CharSequence constraint) {
      return constraint.charAt(0) == '@';
    }
  }
}
