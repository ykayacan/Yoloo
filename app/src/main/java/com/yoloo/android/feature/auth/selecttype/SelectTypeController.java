package com.yoloo.android.feature.auth.selecttype;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.igalata.bubblepicker.BubblePickerListener;
import com.igalata.bubblepicker.model.BubbleGradient;
import com.igalata.bubblepicker.model.PickerItem;
import com.igalata.bubblepicker.rendering.BubblePicker;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.signupinit.SignUpInitController;
import com.yoloo.android.feature.base.BaseController;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SelectTypeController extends BaseController {

  @BindView(R.id.bubblepicker) BubblePicker picker;
  @BindView(R.id.btn_sign_up_init_get_started) TextView tvGetStarted;

  @BindArray(R.array.colors) int[] colors;
  @BindArray(R.array.traveler_types_images) TypedArray travelerTypeDrawables;
  @BindArray(R.array.traveler_types_titles) String[] travelerTypeTitles;
  @BindArray(R.array.traveler_types_ids) String[] travelerTypeIds;

  private ArrayMap<String, String> types = new ArrayMap<>();

  private ArrayList<String> selectedTypeIds = new ArrayList<>();

  public static SelectTypeController create() {
    return new SelectTypeController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_select_type, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    for (int i = 0; i < travelerTypeTitles.length; i++) {
      types.put(travelerTypeTitles[i], travelerTypeIds[i]);
    }

    ArrayList<PickerItem> items = new ArrayList<>();

    picker.setVisibility(View.VISIBLE);

    for (int i = 0; i < travelerTypeTitles.length; i++) {
      Drawable drawable = travelerTypeDrawables.getDrawable(i);
      String title = travelerTypeTitles[i];

      int num = ThreadLocalRandom.current().nextInt(0, 4);

      BubbleGradient gradient = new BubbleGradient(colors[num], colors[num]);

      PickerItem item =
          new PickerItem(title, null, true, null, gradient, 0.5F, Typeface.DEFAULT, Color.WHITE,
              45.0F, drawable);
      items.add(item);
    }

    travelerTypeDrawables.recycle();

    picker.setItems(items);
    picker.setBubbleSize(40);
    picker.setListener(new BubblePickerListener() {
      @Override
      public void onBubbleSelected(PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.add(types.get(pickerItem.getTitle()));

          if (selectedTypeIds.size() >= 3) {
            tvGetStarted.setVisibility(View.VISIBLE);
          }
        }
      }

      @Override
      public void onBubbleDeselected(PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.remove(types.get(pickerItem.getTitle()));

          if (selectedTypeIds.size() < 3) {
            tvGetStarted.setVisibility(View.GONE);
          }
        }
      }
    });
  }

  @OnClick(R.id.btn_sign_up_init_get_started)
  void showSignUpScreen() {
    getRouter().pushController(RouterTransaction
        .with(SignUpInitController.create(selectedTypeIds))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }
}
