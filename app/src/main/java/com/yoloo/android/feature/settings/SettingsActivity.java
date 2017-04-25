package com.yoloo.android.feature.settings;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.yoloo.android.R;

public class SettingsActivity extends MaterialAboutActivity {
  @NonNull
  @Override
  protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
    MaterialAboutTitleItem header = new MaterialAboutTitleItem.Builder()
        .text("Test Title")
        .icon(R.mipmap.ic_launcher)
        .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(context,
            Uri.parse("http://yolooapp.com")))
        .build();

    MaterialAboutCard.Builder builder = new MaterialAboutCard.Builder().addItem(header);

    return new MaterialAboutList(builder.build());
  }

  @Nullable
  @Override
  protected CharSequence getActivityTitle() {
    return "Settings";
  }
}
