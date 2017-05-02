package com.yoloo.android.feature.profile.visitedcountrylist;

import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_country_grid)
public abstract class CountryGridModel
    extends EpoxyModelWithHolder<CountryGridModel.CountryGridHolder> {

  @EpoxyAttribute CountryRealm country;
  @EpoxyAttribute(DoNotHash) RequestManager glide;

  @Override
  public void bind(CountryGridHolder holder) {
    super.bind(holder);
    holder.tvCountryTv.setText(country.getName());

    glide.load(country.getFlagUrl()).into(holder.ivCountryBg);
  }

  @Override
  public void unbind(CountryGridHolder holder) {
    super.unbind(holder);
    Glide.clear(holder.ivCountryBg);
    holder.ivCountryBg.setImageDrawable(null);
  }

  static class CountryGridHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_country_bg) ImageView ivCountryBg;
    @BindView(R.id.tv_country_text) TextView tvCountryTv;
  }
}
