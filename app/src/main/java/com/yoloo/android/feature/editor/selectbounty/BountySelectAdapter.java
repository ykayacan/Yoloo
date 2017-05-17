package com.yoloo.android.feature.editor.selectbounty;

import android.graphics.drawable.Drawable;
import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import com.yoloo.android.util.Group;
import java.util.List;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

class BountySelectAdapter extends SelectableAdapter {

  private final OnBountySelectListener onBountySelectListener;

  BountySelectAdapter(OnBountySelectListener onBountySelectListener) {
    this.onBountySelectListener = onBountySelectListener;
    setMaxSelection(1);
  }

  void addAll(String[] values, List<Drawable> drawables) {
    final int size = values.length;
    for (int i = 0; i < size; i++) {
      addModel(new BountySelectAdapter$BountyModel_()
          .value(values[i])
          .drawable(drawables.get(i))
          .adapter(this)
          .onBountySelectListener(onBountySelectListener));
    }
  }

  void addBounties(List<Group.Of3<Drawable, String, String>> groups) {
    for (Group.Of3<Drawable, String, String> group : groups) {
      addModel(createModel(group));
    }
  }

  private BountySelectAdapter$BountyModel_ createModel(Group.Of3<Drawable, String, String> group) {
    return new BountySelectAdapter$BountyModel_()
        .drawable(group.first)
        .value(group.second)
        .adapter(this)
        .onBountySelectListener(onBountySelectListener);
  }

  void selectBountyItem(int selectedBounty) {
    switch (selectedBounty) {
      case 10:
        toggleSelection(models.get(0));
        break;
      case 20:
        toggleSelection(models.get(1));
        break;
      case 30:
        toggleSelection(models.get(2));
        break;
      case 40:
        toggleSelection(models.get(3));
        break;
      case 50:
        toggleSelection(models.get(4));
        break;
    }
  }

  public interface OnBountySelectListener {
    void onBountySelect(int value);
  }

  @EpoxyModelClass(layout = R.layout.item_bounty_select)
  abstract static class BountyModel extends EpoxyModel<TextView> {

    @EpoxyAttribute(DoNotHash) BountySelectAdapter adapter;
    @EpoxyAttribute String value;
    @EpoxyAttribute(DoNotHash) Drawable drawable;
    @EpoxyAttribute(DoNotHash) OnBountySelectListener onBountySelectListener;

    @Override
    public void bind(TextView view) {
      view.setText(value);
      view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

      final boolean isSelected = adapter.isSelected(this);
      view.setSelected(isSelected);

      view.setOnClickListener(v -> {
        if (adapter.canSelect(this)) {
          adapter.toggleSelection(this);
          onBountySelectListener.onBountySelect(Integer.parseInt(value));
        }
      });
    }
  }
}
