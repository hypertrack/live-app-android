package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 09/03/17.
 */

public class CheckPermission extends BaseActivity {

    private static final String TAG = CheckPermission.class.getSimpleName();


    RelativeLayout parentLayout;
    Button letsGO;
    ProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        //BindViews

        initUI();

    }

    public void initUI() {
        parentLayout = (RelativeLayout) findViewById(R.id.parent_layout);
        letsGO = (Button) findViewById(R.id.lets_go);
        letsGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.permission_image_loader);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && HyperTrack.checkLocationServices(this)) {
            letsGO.setVisibility(View.INVISIBLE);
            letsGO.setOnClickListener(null);
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    proceedToProfileScreen();
                }
            }, 2000);
        }

    }

    public void checkPermission() {

        if (HyperTrack.checkLocationPermission(this)) {
            if (HyperTrack.checkLocationServices(this)) {
                proceedToProfileScreen();
            } else {
                requestLocation();
            }
        } else {
            HyperTrack.requestPermissions(this);
        }
    }

    public void requestLocation() {
        HyperTrack.requestLocationServices(CheckPermission.this, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {

            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                showSnackBar();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case HyperTrack.REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    showSnackBar();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {

            switch (resultCode) {
                case Activity.RESULT_OK:

                    Log.i(TAG, "User agreed to make required location settings changes.");
                    Log.d(TAG, "Fetching Location started!");
                    proceedToProfileScreen();
                    // Perform EnterDestinationLayout Click on Location Enabled, if user clicked on it

                    break;

                case Activity.RESULT_CANCELED:

                    // Location Service Enable Request denied, boo! Fire LocationDenied event
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    showSnackBar();

                    break;
            }
        }
    }

    private void showSnackBar() {
        Snackbar.make(findViewById(R.id.parent_layout), "Enable Location", Snackbar.LENGTH_INDEFINITE).setAction("Enable", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        }).show();
    }


    private void proceedToProfileScreen() {
        startActivity(new Intent(CheckPermission.this, Profile.class));
        finish();
    }
}
