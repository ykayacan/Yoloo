package com.yoloo.android.feature.ui.widget.linkabletextview.annotation;

import com.yoloo.android.feature.ui.widget.linkabletextview.LinkableTextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@LinkTypeDef({
    LinkableTextView.Link.HASH_TAG,
    LinkableTextView.Link.MENTION,
    LinkableTextView.Link.EMAIL_ADDRESS,
    LinkableTextView.Link.PHONE,
    LinkableTextView.Link.WEB_URL,
    LinkableTextView.Link.IP_ADDRESS
})
@Retention(RetentionPolicy.SOURCE)
public @interface LinkType {
}
