package com.yoloo.android.feature.ui.widget.badgeview;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.yoloo.android.R;
import com.yoloo.android.util.DisplayUtil;

public class MenuItemBadge {

  public static void update(final Activity activity, final MenuItem menu, Builder builder) {
    update(activity, menu, builder, null);
  }

  public static void update(final Activity activity, final MenuItem menu, Builder builder,
      final ActionItemBadgeListener listener) {
    if (menu == null) return;

    FrameLayout badge = (FrameLayout) menu.getActionView();
    if (badge != null) {
      MaterialBadgeTextView badgeTextView =
          (MaterialBadgeTextView) badge.findViewById(R.id.menu_badge);
      ImageView imageView = (ImageView) badge.findViewById(R.id.menu_badge_icon);

      //Display icon in ImageView
      if (imageView != null && builder != null) {
        if (builder.iconDrawable != null) {
          imageView.setImageDrawable(builder.iconDrawable);
        }

        if (builder.iconTintColor != Color.TRANSPARENT) {
          imageView.setColorFilter(builder.iconTintColor);
        }
      }

      if (badgeTextView != null
          && builder != null
          && builder.textBackgroundColor != Color.TRANSPARENT) {
        badgeTextView.setBackgroundColor(builder.textBackgroundColor);
      }

      if (badgeTextView != null && builder != null && builder.textColor != Color.TRANSPARENT) {
        badgeTextView.setTextColor(builder.textColor);
      }

      //Bind onOptionsItemSelected to the activity
      if (activity != null) {
        badge.setOnClickListener(v -> {
          boolean consumed = false;
          if (listener != null) {
            consumed = listener.onOptionsItemSelected(menu);
          }
          if (!consumed) {
            activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, menu);
          }
        });

        badge.setOnLongClickListener(v -> {
          Display display = activity.getWindowManager().getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
          int width = size.x;
          Toast toast = Toast.makeText(activity, menu.getTitle(), Toast.LENGTH_SHORT);
          toast.setGravity(Gravity.TOP, width / 5, DisplayUtil.dpToPx(48));
          toast.show();
          return true;
        });
      }

      menu.setVisible(true);
    }
  }

  public static MaterialBadgeTextView getBadgeTextView(MenuItem menu) {
    if (menu == null) {
      return null;
    }
    FrameLayout badge = (FrameLayout) menu.getActionView();
    return (MaterialBadgeTextView) badge.findViewById(R.id.menu_badge);
  }

  /**
   * hide the given menu item
   */
  public static void hide(MenuItem menu) {
    menu.setVisible(false);
  }

  public static String formatNumber(int value, boolean limitLength) {
    if (value < 0) {
      return "-" + formatNumber(-value, limitLength);
    } else if (value < 100) {
      return Long.toString(value);
    } else {
      return "99+";
    }
  }

  public interface ActionItemBadgeListener {
    boolean onOptionsItemSelected(MenuItem menu);
  }

  public static class Builder {

    int textBackgroundColor; // TRANSPARENT = 0;
    int textColor; // TRANSPARENT = 0;
    int iconTintColor; // TRANSPARENT = 0;
    private Drawable iconDrawable;

    public Builder() {
    }

    public Builder textBackgroundColor(int textBackgroundColor) {
      this.textBackgroundColor = textBackgroundColor;
      return this;
    }

    public Builder textColor(int textColor) {
      this.textColor = textColor;
      return this;
    }

    public Builder iconTintColor(int iconTintColor) {
      this.iconTintColor = iconTintColor;
      return this;
    }

    public Builder iconDrawable(Drawable iconDrawable) {
      this.iconDrawable = iconDrawable;
      return this;
    }
  }
}
