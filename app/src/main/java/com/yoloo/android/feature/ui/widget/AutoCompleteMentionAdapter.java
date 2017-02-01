package com.yoloo.android.feature.ui.widget;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AutoCompleteMentionAdapter extends ArrayAdapter<AccountRealm> {

  private static final Pattern MENTION_PATTERN =
      Pattern.compile("@([\\u0041-\\u005A\\u0061-\\u007A\\u00AA\\u00B5\\u00BA\\u00C0" +
          "-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02C1\\u02C6-\\u02D1\\u02E0-\\u02E4\\u02EC" +
          "\\u02EE\\u0370-\\u0374\\u0376\\u0377\\u037A-\\u037D\\u0386\\u0388-\\u038A" +
          "\\u038C\\u038E-\\u03A1\\u03A3-\\u03F5\\u03F7-\\u0481\\u048A-\\u0527\\u0531" +
          "-\\u0556\\u0559\\u0561-\\u0587\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0620-\\u064A" +
          "\\u066E\\u066F\\u0671-\\u06D3\\u06D5\\u06E5\\u06E6\\u06EE\\u06EF\\u06FA-\\u06FC" +
          "\\u06FF\\u0710\\u0712-\\u072F\\u074D-\\u07A5\\u07B1\\u07CA-\\u07EA\\u07F4" +
          "\\u07F5\\u07FA\\u0800-\\u0815\\u081A\\u0824\\u0828\\u0840-\\u0858\\u08A0" +
          "\\u08A2-\\u08AC\\u0904-\\u0939\\u093D\\u0950\\u0958-\\u0961\\u0971-\\u0977" +
          "\\u0979-\\u097F\\u0985-\\u098C\\u098F\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0" +
          "\\u09B2\\u09B6-\\u09B9\\u09BD\\u09CE\\u09DC\\u09DD\\u09DF-\\u09E1\\u09F0" +
          "\\u09F1\\u0A05-\\u0A0A\\u0A0F\\u0A10\\u0A13-\\u0A28\\u0A2A-\\u0A30\\u0A32" +
          "\\u0A33\\u0A35\\u0A36\\u0A38\\u0A39\\u0A59-\\u0A5C\\u0A5E\\u0A72-\\u0A74" +
          "\\u0A85-\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0\\u0AB2\\u0AB3" +
          "\\u0AB5-\\u0AB9\\u0ABD\\u0AD0\\u0AE0\\u0AE1\\u0B05-\\u0B0C\\u0B0F\\u0B10" +
          "\\u0B13-\\u0B28\\u0B2A-\\u0B30\\u0B32\\u0B33\\u0B35-\\u0B39\\u0B3D\\u0B5C" +
          "\\u0B5D\\u0B5F-\\u0B61\\u0B71\\u0B83\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92" +
          "-\\u0B95\\u0B99\\u0B9A\\u0B9C\\u0B9E\\u0B9F\\u0BA3\\u0BA4\\u0BA8-\\u0BAA\\u0BAE" +
          "-\\u0BB9\\u0BD0\\u0C05-\\u0C0C\\u0C0E-\\u0C10\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35" +
          "-\\u0C39\\u0C3D\\u0C58\\u0C59\\u0C60\\u0C61\\u0C85-\\u0C8C\\u0C8E-\\u0C90\\u0C92" +
          "-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CBD\\u0CDE\\u0CE0\\u0CE1\\u0CF1\\u0CF2" +
          "\\u0D05-\\u0D0C\\u0D0E-\\u0D10\\u0D12-\\u0D3A\\u0D3D\\u0D4E\\u0D60\\u0D61\\u0D7A" +
          "-\\u0D7F\\u0D85-\\u0D96\\u0D9A-\\u0DB1\\u0DB3-\\u0DBB\\u0DBD\\u0DC0-\\u0DC6\\u0E01" +
          "-\\u0E30\\u0E32\\u0E33\\u0E40-\\u0E46\\u0E81\\u0E82\\u0E84\\u0E87\\u0E88\\u0E8A" +
          "\\u0E8D\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA\\u0EAB" +
          "\\u0EAD-\\u0EB0\\u0EB2\\u0EB3\\u0EBD\\u0EC0-\\u0EC4\\u0EC6\\u0EDC-\\u0EDF\\u0F00" +
          "\\u0F40-\\u0F47\\u0F49-\\u0F6C\\u0F88-\\u0F8C\\u1000-\\u102A\\u103F\\u1050" +
          "-\\u1055\\u105A-\\u105D\\u1061\\u1065\\u1066\\u106E-\\u1070\\u1075-\\u1081" +
          "\\u108E\\u10A0-\\u10C5\\u10C7\\u10CD\\u10D0-\\u10FA\\u10FC-\\u1248\\u124A" +
          "-\\u124D\\u1250-\\u1256\\u1258\\u125A-\\u125D\\u1260-\\u1288\\u128A-\\u128D" +
          "\\u1290-\\u12B0\\u12B2-\\u12B5\\u12B8-\\u12BE\\u12C0\\u12C2-\\u12C5\\u12C8" +
          "-\\u12D6\\u12D8-\\u1310\\u1312-\\u1315\\u1318-\\u135A\\u1380-\\u138F\\u13A0" +
          "-\\u13F4\\u1401-\\u166C\\u166F-\\u167F\\u1681-\\u169A\\u16A0-\\u16EA\\u1700" +
          "-\\u170C\\u170E-\\u1711\\u1720-\\u1731\\u1740-\\u1751\\u1760-\\u176C\\u176E" +
          "-\\u1770\\u1780-\\u17B3\\u17D7\\u17DC\\u1820-\\u1877\\u1880-\\u18A8\\u18AA" +
          "\\u18B0-\\u18F5\\u1900-\\u191C\\u1950-\\u196D\\u1970-\\u1974\\u1980-\\u19AB" +
          "\\u19C1-\\u19C7\\u1A00-\\u1A16\\u1A20-\\u1A54\\u1AA7\\u1B05-\\u1B33\\u1B45" +
          "-\\u1B4B\\u1B83-\\u1BA0\\u1BAE\\u1BAF\\u1BBA-\\u1BE5\\u1C00-\\u1C23\\u1C4D" +
          "-\\u1C4F\\u1C5A-\\u1C7D]+)");

  private final OnMentionFilterListener onMentionFilterListener;
  private final List<AccountRealm> items;

  private LayoutInflater inflater;

  public AutoCompleteMentionAdapter(Context context,
      OnMentionFilterListener onMentionFilterListener) {
    super(context, 0);
    this.onMentionFilterListener = onMentionFilterListener;
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
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        items.clear();

        if (isConditionsValid(constraint)) {
          final String filtered = constraint.toString().toLowerCase().trim();
          onMentionFilterListener.onMentionFilter(filtered);
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
        if (resultValue instanceof AccountRealm) {
          return ((AccountRealm) resultValue).toMention();
        }
        return super.convertResultToString(resultValue);
      }
    };
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    // Get the data item from filtered search.
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

  public void setItems(List<AccountRealm> accounts) {
    this.items.clear();
    this.items.addAll(accounts);
  }

  private boolean isConditionsValid(CharSequence constraint) {
    return !TextUtils.isEmpty(constraint)
        && hasMentionSymbol(constraint)
        && constraint.length() > 2;
  }

  private boolean hasMentionSymbol(CharSequence constraint) {
    return constraint.charAt(0) == '@';
  }

  public interface OnMentionFilterListener {
    void onMentionFilter(String query);
  }

  private static class MentionViewHolder {
    ImageView ivAvatar;
    TextView tvUsername;
  }
}
