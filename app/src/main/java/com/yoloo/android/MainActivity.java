package com.yoloo.android;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private Router mRouter;

    @BindView(R.id.controller_container)
    ViewGroup mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mRouter = Conductor.attachRouter(this, mContainer, savedInstanceState);

        if (!mRouter.hasRootController()) {
            mRouter.setRoot(RouterTransaction.with(new Controller() {
                @NonNull
                @Override
                protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
                    return null;
                }
            }));
        }
    }

    @Override
    public void onBackPressed() {
        if (!mRouter.handleBack()) {
            super.onBackPressed();
        }
    }
}
