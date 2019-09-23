package com.hypertrack.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.sdk.HyperTrack;

import java.util.concurrent.TimeUnit;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        if (HyperTrack.isTracking()) {
            startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            finish();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                    finish();
                }
            }, TimeUnit.SECONDS.toMillis(1));
        }
    }
}
