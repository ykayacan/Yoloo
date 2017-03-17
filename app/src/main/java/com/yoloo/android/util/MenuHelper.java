package com.yoloo.android.util;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.View;

public final class MenuHelper {

  private MenuHelper() {
    // empty constructor
  }

  public static PopupMenu createMenu(Context context, View view, @MenuRes int menuRes) {
    final PopupMenu popupMenu = new PopupMenu(context, view);
    final MenuInflater inflater = popupMenu.getMenuInflater();
    inflater.inflate(menuRes, popupMenu.getMenu());
    popupMenu.show();
    return popupMenu;
  }
}
