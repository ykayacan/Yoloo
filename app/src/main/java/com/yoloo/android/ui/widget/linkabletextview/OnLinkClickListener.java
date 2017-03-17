package com.yoloo.android.ui.widget.linkabletextview;

public interface OnLinkClickListener {
  void onHashtagClick(String hashtag);

  void onMentionClick(String mention);

  void onEmailAddressClick(String email);

  void onWebUrlClick(String url);

  void onPhoneClick(String phone);
}
