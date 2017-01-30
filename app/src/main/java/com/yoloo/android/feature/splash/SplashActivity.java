package com.yoloo.android.feature.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.yoloo.android.feature.base.BaseActivity;

public class SplashActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = new Intent(this, BaseActivity.class);
    startActivity(intent);
    finish();
  }
}
