package com.yoloo.android.feature;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.yoloo.android.R;
import timber.log.Timber;

/**
 * Activity for displaying information about a receive App Invite invitation.  This activity
 * displays as a Dialog over the MainActivity and does not cover the full screen.
 */
public class DeepLinkActivity extends AppCompatActivity implements View.OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.deep_link_activity);

    findViewById(R.id.button_ok).setOnClickListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Check if the intent contains an AppInvite and then process the referral information.
    Intent intent = getIntent();
    if (AppInviteReferral.hasReferral(intent)) {
      processReferralIntent(intent);
    }
  }

  private void processReferralIntent(Intent intent) {
    // Extract referral information from the intent
    String invitationId = AppInviteReferral.getInvitationId(intent);
    String deepLink = AppInviteReferral.getDeepLink(intent);

    // Display referral information
    Timber.d("Found Referral: %s:%s", invitationId, deepLink);
    ((TextView) findViewById(R.id.deep_link_text)).setText(
        getString(R.string.deep_link_fmt, deepLink));
    ((TextView) findViewById(R.id.invitation_id_text)).setText(
        getString(R.string.invitation_id_fmt, invitationId));
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.button_ok) {
      finish();
    }
  }
}
